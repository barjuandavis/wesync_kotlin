package com.wesync.util

import android.content.Intent.getIntent
import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection



class ServiceManager {
    private val connections = Array<ServiceConnection>(10)
    private val services = arrayOfNulls<MyService>(10)

    private fun connect() {
        val ctxt = getApplicationContext()
        for (i in connections.indices) {
            connections[i] = object : ServiceConnection {

                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    services[i] = (binder as LocalBinder).getService()
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    services[i] = null
                }
            }
            ctxt.bindService(getIntent(), connections[i], Context.BIND_AUTO_CREATE)
        }
    }
}