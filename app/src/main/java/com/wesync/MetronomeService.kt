package com.wesync

import android.app.Service
import android.content.Intent
import android.os.IBinder

object MetronomeService: Service(), Runnable {
    private lateinit var binder: IBinder
    

    override fun onBind(p0: Intent?): IBinder? {
        return binder;
    }

    override fun run() {

    }

    private var bpm: Int = 120


}