package ng.mrlectus.koplerkt

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ScreenWhispererService : AccessibilityService() {

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val mainHandler = Handler(Looper.getMainLooper())
    private var bubble: View? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams

    override fun onServiceConnected() {
        super.onServiceConnected()
        runningService = WeakReference(this)
        applyBubbleVisibility()
        Log.d(TAG, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        keepBubbleOnScreen()
    }

    override fun onDestroy() {
        if (runningService?.get() === this) runningService = null
        removeBubble()
        super.onDestroy()
    }

    private fun applyBubbleVisibility() {
        if (explanationOpen) removeBubble() else showBubble()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showBubble() {
        if (bubble != null) return

        val size = dp(56)
        val margin = dp(12)
        val metrics = resources.displayMetrics

        val view = FrameLayout(this).apply {
            background = ContextCompat.getDrawable(this@ScreenWhispererService, R.drawable.bubble_background)
            contentDescription = getString(R.string.bubble_description)
            elevation = dp(8).toFloat()
            addView(
                ImageView(this@ScreenWhispererService).apply {
                    setImageResource(R.drawable.ic_eye)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                },
                FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER),
            )
        }

        bubbleParams = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = metrics.widthPixels - size - margin
            y = metrics.heightPixels - size - dp(96)
        }

        view.setOnTouchListener(BubbleTouchListener())
        windowManager.addView(view, bubbleParams)
        bubble = view
    }

    private fun removeBubble() {
        val view = bubble ?: return
        windowManager.removeView(view)
        bubble = null
    }

    private fun keepBubbleOnScreen() {
        val view = bubble ?: return
        val metrics = resources.displayMetrics
        bubbleParams.x = bubbleParams.x.coerceIn(
            0,
            (metrics.widthPixels - bubbleParams.width).coerceAtLeast(0),
        )
        bubbleParams.y = bubbleParams.y.coerceIn(
            0,
            (metrics.heightPixels - bubbleParams.height).coerceAtLeast(0),
        )
        windowManager.updateViewLayout(view, bubbleParams)
    }

    private fun explainCurrentScreen() {
        val screenText = ScreenTextReader.read(rootInActiveWindow, packageName)
        Log.d(TAG, "Captured ${screenText.length} characters")
        setBubbleVisible(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mainHandler.postDelayed(
                { captureScreenshot(screenText) },
                BUBBLE_HIDE_DELAY_MS,
            )
        } else {
            launchExplanation(screenText, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun captureScreenshot(screenText: String) {
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    val hardwareBuffer = result.hardwareBuffer
                    val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, result.colorSpace)
                    hardwareBuffer.close()
                    val bytes = bitmap?.let { compressScreenshot(it) }
                    Log.d(TAG, "Screenshot bytes: ${bytes?.size ?: 0}")
                    mainHandler.post { launchExplanation(screenText, bytes) }
                }

                override fun onFailure(errorCode: Int) {
                    Log.w(TAG, "Screenshot failed: $errorCode")
                    mainHandler.post { launchExplanation(screenText, null) }
                }
            },
        )
    }

    private fun compressScreenshot(bitmap: Bitmap): ByteArray? {
        val software = try {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } catch (error: Exception) {
            Log.w(TAG, "Could not copy screenshot", error)
            null
        }
        bitmap.recycle()
        if (software == null) return null

        val scale = min(1f, MAX_SCREENSHOT_EDGE.toFloat() / max(software.width, software.height))
        val scaled = if (scale < 1f) {
            software.scale(
                (software.width * scale).roundToInt(),
                (software.height * scale).roundToInt(),
            ).also { software.recycle() }
        } else {
            software
        }

        val bytes = ByteArrayOutputStream().use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, SCREENSHOT_QUALITY, stream)
            scaled.recycle()
            stream.toByteArray()
        }
        return bytes.takeIf { it.isNotEmpty() }
    }

    private fun launchExplanation(screenText: String, screenshot: ByteArray?) {
        val intent = Intent(this, ExplanationActivity::class.java)
            .putExtra(ExplanationActivity.EXTRA_SCREEN_TEXT, screenText)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (screenshot != null) {
            intent.putExtra(ExplanationActivity.EXTRA_SCREENSHOT, screenshot)
        }

        try {
            startActivity(intent)
        } catch (error: Exception) {
            Log.e(TAG, "Could not open explanation", error)
            setBubbleVisible(true)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private inner class BubbleTouchListener : View.OnTouchListener {

        private var startX = 0
        private var startY = 0
        private var downRawX = 0f
        private var downRawY = 0f
        private var dragging = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = bubbleParams.x
                    startY = bubbleParams.y
                    downRawX = event.rawX
                    downRawY = event.rawY
                    dragging = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    if (!dragging && (abs(dx) > dp(6) || abs(dy) > dp(6))) dragging = true
                    if (dragging) {
                        bubbleParams.x = startX + dx.roundToInt()
                        bubbleParams.y = startY + dy.roundToInt()
                        windowManager.updateViewLayout(view, bubbleParams)
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        keepBubbleOnScreen()
                    } else {
                        view.performClick()
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        explainCurrentScreen()
                    }
                    return true
                }

                MotionEvent.ACTION_CANCEL -> {
                    if (dragging) keepBubbleOnScreen()
                    return true
                }
            }
            return false
        }
    }

    companion object {
        private const val TAG = "ScreenWhisperer"
        private const val BUBBLE_HIDE_DELAY_MS = 150L
        private const val MAX_SCREENSHOT_EDGE = 1280
        private const val SCREENSHOT_QUALITY = 82
        private var runningService: WeakReference<ScreenWhispererService>? = null
        private var explanationOpen = false

        fun setBubbleVisible(visible: Boolean) {
            explanationOpen = !visible
            runningService?.get()?.applyBubbleVisibility()
        }
    }
}
