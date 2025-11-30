package com.ajpr00.tabletapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyReleaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}