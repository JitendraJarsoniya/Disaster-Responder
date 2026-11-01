package com.example.byhm.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.byhm.MainActivity
import com.example.byhm.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class DisasterFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "New FCM device token generated: $token")
        // Token registration with backend if authenticated
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Official Emergency Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["description"] ?: "New disaster advisory issued."
        val alertId = remoteMessage.data["alert_id"]

        showNotification(title, body, alertId)
    }

    private fun showNotification(title: String, body: String, alertId: String?) {
        val channelId = "official_disaster_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Official Disaster Alerts & Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority emergency notifications from NDMA, IMD & CWC"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            alertId?.let { putExtra("EXTRA_ALERT_ID", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun subscribeToPublicAlertsTopic() {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic("disaster_alerts")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FCM_SERVICE", "Subscribed to public disaster_alerts topic anonymously.")
                        }
                    }
            } catch (e: Exception) {
                Log.w("FCM_SERVICE", "FCM topic subscription pending google-services.json configuration.")
            }
        }
    }
}
