package com.wesync.metronome

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import com.wesync.util.service.ForegroundServiceLauncher
import com.wesync.util.MetronomeCodes
import com.wesync.MainActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.wesync.R
import com.wesync.util.service.ForegroundNotification


class MetronomeService: LifecycleService() {

    companion object {
        private val LAUNCHER = ForegroundServiceLauncher(MetronomeService::class.java)
        @JvmStatic
        fun start(context: Context) = LAUNCHER.startService(context)
        @JvmStatic
        fun stop(context: Context) = LAUNCHER.stopService(context)
    }

    private val _binder                                 = LocalBinder()
    private val CHANNEL_ID                              = "wesync_notification_bar"
    private lateinit var notification  : Notification
    private lateinit var handlerThread : TickHandlerThread
    private var bpm                    :Long            = 120
    private var isPlaying                               = false

    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(ForegroundNotification.NOTIFICATION_ID,
            ForegroundNotification.getNotification(this))
        LAUNCHER.onServiceCreated(this)
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
    override fun onBind(intent: Intent): IBinder? {
        handlerThread = TickHandlerThread(applicationContext)
        handlerThread.start()
        super.onBind(intent)
        return this._binder
    }
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("_con","MetronomeService DISCONNECTED")
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    fun play(){
        isPlaying = true
        handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.START_METRONOME)
     }
    fun stop() {
        isPlaying = false
        handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.STOP_METRONOME)
    }
    fun setBPM(bpm: Long) {
        if (handlerThread.isAlive) {
            val m = Message()
            m.what = MetronomeCodes.ON_BPM_CHANGED
            m.obj = bpm
            handlerThread.getHandler().sendMessage(m)
            this.bpm = bpm
        }
    }

    private fun cleanup() {
        handlerThread.quitSafely()
        stopSelf()
    }


}