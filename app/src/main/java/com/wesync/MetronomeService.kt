package com.wesync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Vibrator

object MetronomeService: Service(), Runnable {
    //TODO: make LiveData for configuration changes
    private val binder = LocalBinder()
    private lateinit var vibrator: Vibrator

    private class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return MetronomeService
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun run() {
        //run this service on the background according to the config


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
        vibrator.vibrate(500)
    }

    private fun playFlashScreen() {
        //TODO: find out how to communicate to the UI thread safely (and with the least latency possible)
    }

    private var bpm: Int = 120


}