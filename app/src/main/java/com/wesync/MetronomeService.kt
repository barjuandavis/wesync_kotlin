package com.wesync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Vibrator

object MetronomeService: Service(), Runnable {
    //TODO: make LiveData for configuration changes
    private lateinit var binder: IBinder
    private lateinit var vibrator: Vibrator


    override fun onBind(p0: Intent?): IBinder? {
        return binder;
    }

    override fun run() {

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
        vibrator.vibrate()

        
    }

    private var bpm: Int = 120


}