package io.mehow.squashit.screenshot.capture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.mehow.squashit.R

internal class CaptureService : Service() {
  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val notifications = getSystemService<NotificationManager>()!!
    if (Build.VERSION.SDK_INT >= 26 && notifications.getNotificationChannel(ChannelId) == null) {
      val channel = NotificationChannel(ChannelId, "SquashIt", IMPORTANCE_LOW)
      notifications.createNotificationChannel(channel)
    }
    startForeground(
        NotificationId,
        NotificationCompat.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.squash_it_screenshot_capture)
            .setColor(ContextCompat.getColor(this, R.color.squash_it_royal_blue_300))
            .build()
    )

    return START_NOT_STICKY
  }

  companion object {
    private const val ChannelId = "screenshot=capture"
    private const val NotificationId = 1

    fun start(context: Context) {
      val intent = Intent(context, CaptureService::class.java)
      context.startService(intent)
    }

    fun stop(context: Context) {
      val intent = Intent(context, CaptureService::class.java)
      context.stopService(intent)
    }
  }
}
