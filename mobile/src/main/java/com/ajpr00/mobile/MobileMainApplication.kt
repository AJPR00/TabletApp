package com.ajpr00.mobile

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import javax.inject.Inject
import dagger.hilt.android.HiltAndroidApp

import com.ajpr00.core.util.CoreLog
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

@HiltAndroidApp
class MobileMainApplication : Application()
//    Configuration.Provider
{

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        CoreLog.debug = { tag, msg ->
            android.util.Log.d(tag, msg)
        }

        CoreLog.error = { tag, msg ->
            android.util.Log.e(tag, msg)
        }

        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)
    }

    /*override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()*/
}
