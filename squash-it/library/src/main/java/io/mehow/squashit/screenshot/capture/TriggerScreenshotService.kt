package io.mehow.squashit.screenshot.capture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.mehow.squashit.R

internal class TriggerScreenshotService : Service() {
  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val notifications = getSystemService<NotificationManager>()!!
    if (Build.VERSION.SDK_INT >= 26 && notifications.getNotificationChannel(ChannelId) == null) {
      val channel = NotificationChannel(ChannelId, "SquashIt", IMPORTANCE_LOW)
      notifications.createNotificationChannel(channel)
    }

    val triggerIntent = PendingIntent.getBroadcast(this, 0, TriggerScreenshotReceiver.intent(), 0)
    startForeground(
        NotificationId,
        NotificationCompat.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.squash_it_ic_logo)
            .setColor(ContextCompat.getColor(this, R.color.squash_it_royal_blue_300))
            .setContentTitle(getString(R.string.squash_it_report_notification_title))
            .setContentText(getString(R.string.squash_it_report_notification_message))
            .setContentIntent(triggerIntent)
            .build()
    )
    return START_NOT_STICKY
  }

  companion object {
    private const val ChannelId = "screenshot.trigger"
    private const val NotificationId = 2

    fun start(context: Context) {
      val intent = Intent(context, TriggerScreenshotService::class.java)
      context.startService(intent)
    }

    fun stop(context: Context) {
      val intent = Intent(context, TriggerScreenshotService::class.java)
      context.stopService(intent)
    }
  }
}
