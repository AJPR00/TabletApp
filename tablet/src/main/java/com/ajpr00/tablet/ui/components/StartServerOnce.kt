package com.ajpr00.tablet.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ajpr00.tablet.data.server.ServerService

@Composable
fun StartServerOnce(context: Context) {
    LaunchedEffect(Unit) {
        val intent = Intent(context, ServerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
