package com.ajpr00.tablet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TabletApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}