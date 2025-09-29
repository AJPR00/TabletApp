package com.ajpr00.tabletapp

import android.app.Application

// Esta clase se lanza justo al arrancar la app, antes de que se abra ninguna screen

class MyReleaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}