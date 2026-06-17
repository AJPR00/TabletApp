package com.ajpr00.tablet

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TabletMainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)

        FacebookSdk.setIsDebugEnabled(true)
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS)
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS)
        FacebookSdk.addLoggingBehavior(LoggingBehavior.GRAPH_API_DEBUG_INFO)
        FacebookSdk.addLoggingBehavior(LoggingBehavior.GRAPH_API_DEBUG_WARNING)

        // Obtener y mostrar la clave hash de la aplicación
      /*  try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.d("FB_KEY_HASH", "KeyHash: $keyHash")
            }
        } catch (e: Exception) {
            Log.e("FB_KEY_HASH", "Error KeyHash", e)
        }*/

    }
}