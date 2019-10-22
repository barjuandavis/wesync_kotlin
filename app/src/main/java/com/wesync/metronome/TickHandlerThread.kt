package com.wesync.metronome

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import com.wesync.R

class TickHandlerThread( context:Context ): HandlerThread("TickHandlerThread",
    Process.THREAD_PRIORITY_DEFAULT) {

    /*
           TODO: try to reduce audio latency by using Oboe's AudioStream instead of MediaPlayer
     */

    private lateinit var handler: Handler
    private var mp: MediaPlayer = MediaPlayer.create(context, R.raw.tick)
    private var bpm:Long = 120
    private var isPlaying:Boolean? = false

    override fun run() {
        if (Looper.myLooper() == null) {
            super.run()
        }
        else
            Looper.loop()
        mp.prepare()
        Log.d("ThreadStart","Thread has been started!")
    }

    override fun onLooperPrepared() {
        handler = Handler {
            when (it.what) {
            MetronomeCodes.START_METRONOME.v -> {
                this.isPlaying = true
                mp.start()
                SystemClock.sleep(60000 / this.bpm)
                if (this.isPlaying == true) {
                    handler.sendEmptyMessage(MetronomeCodes.START_METRONOME.v)
                }
            }
            MetronomeCodes.STOP_METRONOME.v -> {
                isPlaying = false
                handler.removeMessages(MetronomeCodes.START_METRONOME.v)
                handler.removeMessages(MetronomeCodes.ON_BPM_CHANGED.v)
                handler.removeMessages(MetronomeCodes.STOP_METRONOME.v)
            }
            MetronomeCodes.ON_BPM_CHANGED.v -> {
                this.bpm = it.obj as Long
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