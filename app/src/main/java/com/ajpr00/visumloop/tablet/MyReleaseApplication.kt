package com.ajpr00.visumloop.tablet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyReleaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}