package com.wesync.metronome

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import com.wesync.R
import com.wesync.util.MetronomeCodes
import kotlin.math.roundToLong

class TickHandlerThread(private val context:Context): HandlerThread("TickHandlerThread",
    Process.THREAD_PRIORITY_URGENT_AUDIO) {

    private lateinit var handler: Handler
    private var tickSound: Int = 0
    private var sp: SoundPool  = buildSoundPool()
    private var bpm:Long = 120
    private var _isPlaying:Boolean? = false
    private var preStartLatency: Long = 0

    override fun run() {
        if (Looper.myLooper() == null) {
            super.run()
        }
        else
            Looper.loop()
    }

    override fun onLooperPrepared() {
        handler = Handler {
            when (it.what) {
            MetronomeCodes.START_METRONOME -> {
                _isPlaying = true
                if (preStartLatency > 0) {
                    SystemClock.sleep(preStartLatency)
                }
                handler.sendEmptyMessage(MetronomeCodes.TICK)
            }
            MetronomeCodes.STOP_METRONOME -> {
                _isPlaying = false
                handler.removeMessages(MetronomeCodes.START_METRONOME)
                handler.removeMessages(MetronomeCodes.ON_BPM_CHANGED)
                handler.removeMessages(MetronomeCodes.TICK)
                handler.removeMessages(MetronomeCodes.STOP_METRONOME)
            }
            MetronomeCodes.ON_BPM_CHANGED -> {
                this.bpm = it.obj as Long
            }
            MetronomeCodes.TICK -> {
                sp.play(tickSound,1.0f,1.0f, Thread.MAX_PRIORITY,0,1.0f)
                SystemClock.sleep((60000 / this.bpm).toDouble().roundToLong())
                if (_isPlaying == true) handler.sendEmptyMessage(MetronomeCodes.TICK)
            }
        }
            return@Handler true
        }
    }

    private fun buildSoundPool(): SoundPool {
        val sp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val aa = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(aa)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_MUSIC,0)
        }
        tickSound = sp.load(context,R.raw.tick,1)

        return sp
    }

    fun getHandler(): Handler {
        return handler
    }

    override fun quitSafely(): Boolean {
        sp.release() //please rebuild soundpool to reuse
        return super.quitSafely()
    }

    fun isPlaying() = _isPlaying
}