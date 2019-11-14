package com.wesync.metronome

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import com.wesync.util.ForegroundServiceLauncher
import com.wesync.util.MetronomeCodes
import com.wesync.MainActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.wesync.R


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
    private var bpm                    :Long = 120
    private var isPlaying                               = false

    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, notification)
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

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    fun onPlay(){
        isPlaying = !isPlaying
        if (isPlaying) {
            handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.START_METRONOME)
        }
        else {
            handlerThread.getHandler().sendEmptyMessage(MetronomeCodes.STOP_METRONOME)
        }
     }

     fun onBPMChanged(bpm: Long) {
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Wesync Notification Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager: NotificationManager? = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, notificationIntent, 0
        )
        notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wesync Metronome Notification")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

}