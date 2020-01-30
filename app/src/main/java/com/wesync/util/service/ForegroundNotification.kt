package com.wesync.util.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.*
import androidx.core.content.ContextCompat.getSystemService
import com.wesync.MainActivity
import com.wesync.R

object ForegroundNotification {
    private const val CHANNEL_ID = "com.wesync"
        const val NOTIFICATION_ID = 357951

    fun getNotification(context: Context): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val serviceChannel = NotificationChannel(CHANNEL_ID,
                "Wesync Notification Channel", NotificationManager.IMPORTANCE_LOW)

            val manager :NotificationManager =
                getSystemService(context,NotificationManager::class.java) as NotificationManager

            manager.createNotificationChannel(serviceChannel)

        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Wesync: Synchronized Metronome")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}