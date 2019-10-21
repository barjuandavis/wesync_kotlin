package com.wesync

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.app.Service
import android.os.*


class MetronomeService: Service() {
    /*
          TODO: every call goes DIRECTLY HERE, but every config changes GOES TO MetronomeConfig
     */
    private val binder = LocalBinder()
    private lateinit var handlerThread: TickHandlerThread
    private lateinit var vibrator: Vibrator
    private val START_METRONOME = 100
    private val STOP_METRONOME = 101
    private val ON_BPM_CHANGED = 123
    private var bpm:Long = 120
    private var isPlaying = false

    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        handlerThread = TickHandlerThread(this.applicationContext)
        handlerThread.start()
        return this.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
    }

    fun onPlay(){ //asumsi: pasti dipanggil setelah onBind
        if (!isPlaying) {
            handlerThread.getHandler().sendEmptyMessage(START_METRONOME)
        }
        else {
            handlerThread.getHandler().sendEmptyMessage(STOP_METRONOME)
        }
        isPlaying = !isPlaying //flip the switch
     }

     fun onBPMChanged(bpm: Long) {
        if (handlerThread.isAlive) {
            val m = Message()
            m.what = ON_BPM_CHANGED
            m.obj = bpm
            handlerThread.getHandler().sendMessage(m)
            this.bpm = bpm
        }

    }

    private fun playFlashScreen() {
        //TODO: find out how to communicate to the UI thread safely and with the least latency possible. NOTE: might not make it to the release build
    }

    private fun cleanup() {
        handlerThread.quitSafely()
    }


}