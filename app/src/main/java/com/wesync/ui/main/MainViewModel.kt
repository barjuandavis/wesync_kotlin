package com.wesync.ui.main

import android.app.Application
import android.content.ComponentName

import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import com.wesync.MetronomeService


class MainViewModel(application: Application) : AndroidViewModel(application) {
    //start the service from here
    private lateinit var mService: MetronomeService
    private var mBound: Boolean = false
    var metronomeService: MetronomeService = MetronomeService()
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MetronomeService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    fun onPlayButtonPressed() {

    }



}
