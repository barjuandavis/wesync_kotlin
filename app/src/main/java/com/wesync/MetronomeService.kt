package com.wesync

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import java.util.*


class MetronomeService: IntentService(MetronomeService::class.simpleName) {

    private val binder = LocalBinder()
    private lateinit var vibrator: Vibrator
    private lateinit var configObserver: ConfigObserver
    private var intent:Intent? = null
    private lateinit var mainTimer: Timer
    private lateinit var subTimer: Timer
    private val MILLIS_IN_MINUTE:Long = 60000
    private var bpm: Long = 120


    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return this.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }

    override fun onHandleIntent(workIntent: Intent) {
        Log.d("onHandleIntent","Is it handled?")
        this.intent = workIntent
        val dataString = workIntent.extras

        when (dataString.get("command")) {
            "PLAY_METRONOME" -> {
                playTheWholeThing()
            }
            "PAUSE_METRONOME" -> {
                stopTheWholeThing()
            }
        }
    }

    private fun playTheWholeThing() {
        Log.d("PLAY_METRONOME","Playing.")
        mainTimer = Timer()
        subTimer = Timer()
        val mainTimerTask = MyTimerTask()
        val subTimerTask = MyTimerTask()
        mainTimer.schedule(mainTimerTask, 0, MILLIS_IN_MINUTE / bpm)
        subTimer.schedule(subTimerTask, 300 * (100) / bpm, MILLIS_IN_MINUTE / bpm)
    }

    private fun stopTheWholeThing() {
        mainTimer.cancel()
        subTimer.cancel()
        mainTimer.purge()
        subTimer.purge()
    }

    private fun playSound() {
        //TODO: check if sound is available in the config
        val mp = MediaPlayer.create(this,R.raw.tick)
        mp.start()
        mp.setOnCompletionListener { mp.release() }
    }

    private fun playVibrate() {
        //TODO: check if vibrate is available in the config
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50)
    }

    private fun playFlashScreen() {
        //TODO: find out how to communicate to the UI thread safely (and with the least latency possible)
    }

    inner class MyTimerTask: TimerTask() {
        override fun run() {
            this@MetronomeService.playSound()
            this@MetronomeService.playVibrate()
        }
    }


}