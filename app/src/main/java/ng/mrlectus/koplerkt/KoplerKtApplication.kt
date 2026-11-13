package ng.mrlectus.koplerkt

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

class KoplerKtApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )

        Firebase.appCheck.getAppCheckToken(false)
            .addOnSuccessListener { Log.d(TAG, "App Check token fetched") }
            .addOnFailureListener { error -> Log.e(TAG, "App Check token fetch failed", error) }
    }

    private companion object {
        const val TAG = "KoplerKtApplication"
    }
}
