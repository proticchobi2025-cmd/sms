package com.example.smssheettracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smssheettracker.R
import com.example.smssheettracker.domain.TransactionRepository

class SmsForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        TransactionRepository.scheduleSync(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        TransactionRepository.scheduleSync(this)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SMS Tracker Service",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("SMS Tracker Running")
        .setContentText("Monitoring financial SMS and syncing with Google Sheets")
        .setSmallIcon(R.drawable.ic_stat_sms)
        .setOngoing(true)
        .build()

    companion object {
        private const val CHANNEL_ID = "sms_tracker_channel"
        private const val NOTIFICATION_ID = 1011
    }
}
