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
    var isPlaying = false

    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("onBind","service bound to:" + p0.toString())
        Log.d("pid","App created on pid:"+Thread.currentThread().id)
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
            Log.d("playing","playing")
            handlerThread.start()
            handlerThread.getHandler().sendEmptyMessage(START_METRONOME)
        }
        else {
            Log.d("stopping","stopping")
            handlerThread.getHandler().sendEmptyMessage(STOP_METRONOME)
        }
        isPlaying = !isPlaying //flip the switch
     }

    private fun playFlashScreen() {
        //TODO: find out how to communicate to the UI thread safely and with the least latency possible. NOTE: might not make it to the release build
    }

    private fun cleanup() {
        handlerThread.quitSafely()
    }


}