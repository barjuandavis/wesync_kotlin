package com.wesync.metronome

import android.content.Intent
import android.app.Service
import android.os.*


class MetronomeService: Service() {
    /*
          TODO: every call goes DIRECTLY HERE, but every config changes GOES TO MetronomeConfig
     */
    private val binder = LocalBinder()
    private lateinit var handlerThread: TickHandlerThread
    private lateinit var vibrator: Vibrator
    private var bpm:Long = 120
    private var isPlaying = false

    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }

    fun isPlaying() = isPlaying

    override fun onBind(p0: Intent?): IBinder? {
        handlerThread = TickHandlerThread(this.applicationContext)
        handlerThread.start()
        return this.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    fun onPlay(){
        if (!isPlaying) {
            handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.START_METRONOME.v)
        }
        else {
            handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.STOP_METRONOME.v)
        }
        isPlaying = !isPlaying //flip the switch
     }

     fun onBPMChanged(bpm: Long) {
        if (handlerThread.isAlive) {
            val m = Message()
            m.what = MetronomeCodes.ON_BPM_CHANGED.v
            m.obj = bpm
            handlerThread.getHandler().sendMessage(m)
            this.bpm = bpm
        }

    }

    private fun cleanup() {
        handlerThread.quitSafely()
    }


}