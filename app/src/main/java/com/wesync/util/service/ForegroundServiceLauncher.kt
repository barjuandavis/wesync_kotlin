package com.wesync.util.service

import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ForegroundServiceLauncher(private val serviceClass: Class<out Service>) {

    private var isStarting = false
    private var shouldStop = false

    @Synchronized
    fun startService(context: Context, block: Intent.() -> Unit = {}) {
        isStarting = true
        shouldStop = false
        ContextCompat.startForegroundService(context, Intent(context, serviceClass).apply { block() })
    }

    @Synchronized
    fun stopService(context: Context) {
        if (isStarting) {
            shouldStop = true
        } else {
            context.stopService(Intent(context, serviceClass))
        }
    }

    @Synchronized
    fun onServiceCreated(service: Service) {
        isStarting = false
        if (shouldStop) {
            shouldStop = false
            service.stopSelf()
        }
    }
}