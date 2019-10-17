package com.wesync

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.util.Log

class TickHandlerThread( context:Context ): HandlerThread("TickHandlerThread",
    Process.THREAD_PRIORITY_DEFAULT) {

    private lateinit var handler: Handler
    private var mp: MediaPlayer = MediaPlayer.create(context,R.raw.tick)
    private val START_METRONOME = 100
    private val STOP_METRONOME = 101
    private var isPlaying = false

    override fun run() {
        if (Looper.myLooper() == null) {
            super.run()
        }
        else
            Looper.loop()

        mp.prepareAsync()
    }

    override fun onLooperPrepared() {
        handler = Handler() { when (it.what) {
            START_METRONOME -> {
                isPlaying = true
                mp.start()
                SystemClock.sleep(60000 / MetronomeConfig.bpm)
                if (isPlaying) {
                    handler.sendEmptyMessage(START_METRONOME)
                }
            }
            STOP_METRONOME -> {
                handler.removeMessages(START_METRONOME)
                isPlaying = false
            }
        }
            return@Handler true
        }
    }

    override fun quitSafely(): Boolean {
        mp.stop()
        mp.release()
        return super.quitSafely()
    }

    fun getHandler(): Handler {
        return handler
    }
}