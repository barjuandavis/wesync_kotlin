package com.wesync.connection

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class ConnectionManagerService : Service() {

    private val _binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService() : ConnectionManagerService {
            return this@ConnectionManagerService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return _binder
    }
}
