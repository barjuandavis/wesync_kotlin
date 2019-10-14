package com.wesync

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.media.AsyncPlayer
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import java.util.*
import android.content.ContentResolver
import android.R.attr.scheme
import sun.security.tools.PathList.appendPath
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioManager
import android.net.Uri
import android.os.Build


class MetronomeService: IntentService(MetronomeService::class.simpleName) {

    /*
          TODO: Make this thing run on a different thread
          TODO: bind this to the fragment
          TODO: every call goes DIRECTLY HERE, but every config changes GOES TO MetronomeConfig
     */

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

    private fun resIdToUri(resourceId: Int): Uri {
        val resources = applicationContext.resources
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()
        return uri
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
        val ap = AsyncPlayer("async_player")
        val aa = AudioAttributes.Builder().
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).
            setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ap.play(this.applicationContext,resIdToUri(R.raw.tick),false, aa)
        } else {
            ap.play(this.applicationContext,resIdToUri(R.raw.tick),false,AudioManager.STREAM_MUSIC)
        }
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