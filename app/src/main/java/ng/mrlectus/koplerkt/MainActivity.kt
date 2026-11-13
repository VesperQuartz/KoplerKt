package ng.mrlectus.koplerkt

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.Firebase
import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.OnDeviceExtension
import com.google.firebase.ai.OnDeviceModelStatus
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(PublicPreviewAPI::class)
class MainActivity : AppCompatActivity() {

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = MODEL_NAME,
                onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE),
            )
    }

    private lateinit var accessibilityPill: TextView
    private lateinit var accessibilityStatus: TextView
    private lateinit var onDevicePill: TextView
    private lateinit var onDeviceStatus: TextView
    private lateinit var cloudFallbackStatus: TextView
    private lateinit var cloudFallbackSwitch: SwitchMaterial

    private var pollJob: Job? = null
    private var downloadJob: Job? = null
    private var downloadAttempted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accessibilityPill = findViewById(R.id.accessibility_pill)
        accessibilityStatus = findViewById(R.id.accessibility_status)
        onDevicePill = findViewById(R.id.on_device_pill)
        onDeviceStatus = findViewById(R.id.on_device_status)
        cloudFallbackStatus = findViewById(R.id.cloud_fallback_status)
        cloudFallbackSwitch = findViewById(R.id.cloud_fallback_switch)

        findViewById<Button>(R.id.open_accessibility_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.open_demo).setOnClickListener {
            startActivity(Intent(this, DemoScamActivity::class.java))
        }
        cloudFallbackSwitch.setOnCheckedChangeListener { _, allowed ->
            CloudPreferences.setAllowed(this, allowed)
            updateCloudFallbackStatus(allowed)
        }
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
        syncCloudFallback()
        refreshOnDeviceModel()
    }

    override fun onPause() {
        pollJob?.cancel()
        super.onPause()
    }

    private fun updateAccessibilityStatus() {
        val enabled = isAccessibilityServiceEnabled()
        accessibilityStatus.setText(
            if (enabled) R.string.accessibility_body_on else R.string.accessibility_body_off,
        )
        setPill(
            accessibilityPill,
            if (enabled) R.string.accessibility_pill_on else R.string.accessibility_pill_off,
            if (enabled) R.drawable.pill_green else R.drawable.pill_gray,
            if (enabled) R.color.on_device_green else R.color.whisper_muted,
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()

        return enabledServices.split(':').any {
            it.startsWith("$packageName/") && it.contains(SERVICE_CLASS_NAME)
        }
    }

    private fun syncCloudFallback() {
        val allowed = CloudPreferences.isAllowed(this)
        if (cloudFallbackSwitch.isChecked != allowed) {
            cloudFallbackSwitch.isChecked = allowed
        }
        updateCloudFallbackStatus(allowed)
    }

    private fun updateCloudFallbackStatus(allowed: Boolean) {
        cloudFallbackStatus.setText(
            if (allowed) R.string.cloud_fallback_body_on else R.string.cloud_fallback_body_off,
        )
    }

    private fun refreshOnDeviceModel() {
        pollJob?.cancel()
        if (downloadJob?.isActive != true) {
            downloadAttempted = false
        }
        pollJob = lifecycleScope.launch {
            val onDevice = model.onDeviceExtension
            if (onDevice == null) {
                showOnDeviceUnavailable()
                return@launch
            }

            while (isActive) {
                val status = try {
                    onDevice.checkStatus()
                } catch (error: Exception) {
                    Log.e(TAG, "On-device model check failed", error)
                    showOnDeviceUnavailable()
                    return@launch
                }

                Log.d(TAG, "On-device model status: $status")
                when (status) {
                    OnDeviceModelStatus.AVAILABLE -> {
                        showOnDeviceReady()
                        return@launch
                    }

                    OnDeviceModelStatus.DOWNLOADABLE -> {
                        showOnDeviceDownloadable()
                        if (!downloadAttempted) {
                            downloadAttempted = true
                            startOnDeviceDownload(onDevice)
                        }
                    }

                    OnDeviceModelStatus.DOWNLOADING -> showOnDeviceDownloading()

                    else -> {
                        showOnDeviceUnavailable()
                        return@launch
                    }
                }

                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun startOnDeviceDownload(onDevice: OnDeviceExtension) {
        downloadJob?.cancel()
        downloadJob = lifecycleScope.launch {
            var totalBytes = 0L
            try {
                onDevice.download().collect { downloadStatus ->
                    when (downloadStatus) {
                        is DownloadStatus.DownloadStarted -> {
                            totalBytes = downloadStatus.bytesToDownload
                            showOnDeviceDownloading(0, totalBytes)
                        }

                        is DownloadStatus.DownloadInProgress ->
                            showOnDeviceDownloading(downloadStatus.totalBytesDownloaded, totalBytes)

                        is DownloadStatus.DownloadCompleted -> showOnDeviceReady()

                        is DownloadStatus.DownloadFailed -> {
                            Log.e(TAG, "On-device model download failed: $downloadStatus")
                            showOnDeviceUnavailable()
                            pollJob?.cancel()
                        }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "On-device model download failed", error)
                showOnDeviceUnavailable()
                pollJob?.cancel()
            }
        }
    }

    private fun showOnDeviceReady() {
        onDeviceStatus.setText(R.string.on_device_body_ready)
        setPill(
            onDevicePill,
            R.string.on_device_pill_ready,
            R.drawable.pill_green,
            R.color.on_device_green,
        )
    }

    private fun showOnDeviceDownloadable() {
        onDeviceStatus.setText(R.string.on_device_body_downloadable)
        setPill(
            onDevicePill,
            R.string.on_device_pill_download,
            R.drawable.pill_blue,
            R.color.whisper_primary,
        )
    }

    private fun showOnDeviceDownloading(downloadedBytes: Long = 0L, totalBytes: Long = 0L) {
        onDeviceStatus.text = when {
            totalBytes > 0 -> getString(
                R.string.on_device_downloading_progress,
                downloadedBytes / BYTES_PER_MB,
                totalBytes / BYTES_PER_MB,
            )

            downloadedBytes > 0 -> getString(
                R.string.on_device_downloading_progress_unknown,
                downloadedBytes / BYTES_PER_MB,
            )

            else -> getString(R.string.on_device_body_downloading)
        }
        setPill(
            onDevicePill,
            R.string.on_device_pill_downloading,
            R.drawable.pill_amber,
            R.color.cloud_amber,
        )
    }

    private fun showOnDeviceUnavailable() {
        onDeviceStatus.setText(R.string.on_device_body_unavailable)
        setPill(
            onDevicePill,
            R.string.on_device_pill_unavailable,
            R.drawable.pill_gray,
            R.color.whisper_muted,
        )
    }

    private fun setPill(
        pill: TextView,
        textRes: Int,
        backgroundRes: Int,
        colorRes: Int,
    ) {
        pill.setText(textRes)
        pill.setBackgroundResource(backgroundRes)
        pill.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private companion object {
        const val MODEL_NAME = "gemini-3.5-flash-lite"
        const val SERVICE_CLASS_NAME = "ScreenWhispererService"
        const val TAG = "MainActivity"
        const val POLL_INTERVAL_MS = 2_000L
        const val BYTES_PER_MB = 1024L * 1024L
    }
}
