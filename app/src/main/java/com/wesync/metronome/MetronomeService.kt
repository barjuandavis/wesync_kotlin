package com.wesync.metronome

import android.content.Intent
import android.app.Service
import android.os.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import com.wesync.MainActivity
import com.wesync.util.MetronomeCodes


class MetronomeService: LifecycleService() {

    private val binder = LocalBinder()
    private lateinit var handlerThread: TickHandlerThread
    private var bpm:Long = 120
    private var isPlaying = false

    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onBind(p0: Intent): IBinder? {
        super.onBind(p0)
        handlerThread = TickHandlerThread(this.applicationContext)
        handlerThread.start()
        return this.binder
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }
    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    fun onPlay(){
        isPlaying = !isPlaying
        if (isPlaying) {
            handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.START_METRONOME)
        }
        else {
            handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.STOP_METRONOME)
        }
     }

     fun onBPMChanged(bpm: Long) {
        if (handlerThread.isAlive) {
            val m = Message()
            m.what = MetronomeCodes.ON_BPM_CHANGED
            m.obj = bpm
            handlerThread.getHandler().sendMessage(m)
            this.bpm = bpm
        }

    }

    private fun cleanup() {
        handlerThread.quitSafely()
    }


}