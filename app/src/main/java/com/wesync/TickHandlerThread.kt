package com.wesync

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.util.Log

class TickHandlerThread( context:Context ): HandlerThread("TickHandlerThread",
    Process.THREAD_PRIORITY_DEFAULT) {

    /*
           TODO: try to reduce audio latency by using Oboe's AudioStream instead of MediaPlayer
     */

    private lateinit var handler: Handler
    private var mp: MediaPlayer = MediaPlayer.create(context,R.raw.tick)
    private var bpm:Long = 120
    private val START_METRONOME = 100
    private val STOP_METRONOME = 101
    private val ON_BPM_CHANGED = 123
    private var isPlaying = true

    override fun run() {
        if (Looper.myLooper() == null) {
            super.run()
        }
        else
            Looper.loop()
        mp.prepareAsync()
        Log.d("ThreadStart","Thread has been started!")
    }

    override fun onLooperPrepared() {
        handler = Handler() {
            when (it.what) {
            START_METRONOME -> {
                this.isPlaying = true
                mp.start()
                Log.d("START_METRONOME","tick = " + this.bpm)
                SystemClock.sleep(60000 / this.bpm)
                if (this.isPlaying) {
                    Log.d("START_METRONOME_resend","Resending tick")
                    handler.sendEmptyMessage(START_METRONOME)
                }
            }
            STOP_METRONOME -> {
                handler.removeMessages(START_METRONOME)
                handler.removeMessages(ON_BPM_CHANGED)
                isPlaying = false
            }
            ON_BPM_CHANGED -> {
                this.bpm = it.obj as Long
                Log.d("ON_CHANGED_BPM","Changed to ${this.bpm}")
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