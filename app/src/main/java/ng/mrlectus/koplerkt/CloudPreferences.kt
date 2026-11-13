package ng.mrlectus.koplerkt

import android.content.Context
import androidx.core.content.edit

object CloudPreferences {

    private const val PREFS = "koplerkt_settings"
    private const val KEY_ALLOWED = "cloud_fallback_allowed"
    private const val KEY_DECIDED = "cloud_fallback_decided"

    fun isAllowed(context: Context): Boolean = prefs(context).getBoolean(KEY_ALLOWED, false)

    fun isDecided(context: Context): Boolean = prefs(context).getBoolean(KEY_DECIDED, false)

    fun setAllowed(context: Context, allowed: Boolean) {
        prefs(context).edit {
            putBoolean(KEY_ALLOWED, allowed)
            putBoolean(KEY_DECIDED, true)
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
