package com.wesync.metronome

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.*
import com.wesync.util.ForegroundServiceLauncher
import com.wesync.util.MetronomeCodes
import android.app.PendingIntent
import android.app.Service
import com.wesync.MainActivity
import androidx.core.app.NotificationCompat
import com.wesync.R


class MetronomeService: Service() {

    companion object {
        private val LAUNCHER = ForegroundServiceLauncher(MetronomeService::class.java)
        @JvmStatic
        fun start(context: Context) = LAUNCHER.startService(context)
        @JvmStatic
        fun stop(context: Context) = LAUNCHER.stopService(context)
    }

    private val binder = LocalBinder()
    private val CHANNEL_ID = "wesync_notification_bar"
    private lateinit var handlerThread: TickHandlerThread
    private var bpm:Long = 120
    private var isPlaying = false

    inner class LocalBinder : Binder() {
        fun getService() : MetronomeService {
            return this@MetronomeService
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wesync Metronome")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(p0: Intent): IBinder? {
        handlerThread = TickHandlerThread(applicationContext)
        handlerThread.start()
        return this.binder
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }
    override fun onUnbind(intent: Intent?): Boolean {
        cleanup()
        return super.onUnbind(intent)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Wesync Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager? = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }


    private fun cleanup() {
        handlerThread.quitSafely()
    }

}