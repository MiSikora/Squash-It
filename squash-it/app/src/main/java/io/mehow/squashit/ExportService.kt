package io.mehow.squashit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.IBinder
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import java.io.File
import javax.inject.Inject

class ExportService : Service() {
  @Inject lateinit var database: Database
  @Inject lateinit var moshi: Moshi

  override fun onCreate() {
    AndroidInjection.inject(this)
  }

  @Suppress("LongMethod")
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val notifications = getSystemService<NotificationManager>()!!
    if (Build.VERSION.SDK_INT >= 26 && notifications.getNotificationChannel(ChannelId) == null) {
      val channelName = getString(R.string.credentials_export)
      val channel = NotificationChannel(ChannelId, channelName, IMPORTANCE_LOW)
      notifications.createNotificationChannel(channel)
    }

    startForeground(
        NotificationId,
        createNotification(
            R.string.export_service_title_creating,
            R.string.export_service_text_creating
        )
    )

    GlobalScope.launch {
      val credentials = database.credentialsQueries.getAll().executeAsList()
      if (credentials.isEmpty()) {
        stopSelf(startId)
        notifications.notify(
            NotificationId,
            createNotification(
                R.string.export_service_title_failed,
                R.string.export_service_text_no_credentials_failure
            )
        )
        return@launch
      }

      val jsonCredentials = credentials.map { JsonCredentials.fromCredentials(it) }
      val moshiType = Types.newParameterizedType(List::class.java, JsonCredentials::class.java)
      val adapter = moshi.adapter<List<JsonCredentials>>(moshiType)

      val exportFile = try {
        val squashItDir = File(cacheDir, "squash-it")
        squashItDir.mkdirs()
        val credentialsFile = File(squashItDir, "credentials.json")

        credentialsFile.sink().buffer().use { sink ->
          sink.writeUtf8(adapter.toJson(jsonCredentials))
        }
        credentialsFile
      } catch (_: Exception) {
        stopSelf(startId)
        notifications.notify(
            NotificationId,
            createNotification(
                R.string.export_service_title_failed,
                R.string.export_service_text_write_file_failure
            )
        )
        return@launch
      }

      val exportUri = exportFile.toCredentialsUri(applicationContext)
      val exportIntent = Intent().apply {
        action = ACTION_SEND
        putExtra(EXTRA_STREAM, exportUri)
        type = "application/json"
        addFlags(FLAG_ACTIVITY_NEW_TASK)
      }
      withContext(Dispatchers.Main) {
        startActivity(exportIntent)
      }
      stopSelf(startId)
    }

    return START_NOT_STICKY
  }

  private fun createNotification(@StringRes title: Int, @StringRes text: Int): Notification {
    return NotificationCompat.Builder(this, ChannelId)
        .setSmallIcon(R.drawable.ic_export)
        .setContentTitle(getString(title))
        .setContentText(getString(text))
        .setColor(ContextCompat.getColor(this, R.color.royal_blue_300))
        .build()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  companion object {
    private const val ChannelId = "credentials-exporter"
    private const val NotificationId = 1

    fun start(context: Context) {
      val intent = Intent(context, ExportService::class.java)
      context.startService(intent)
    }
  }
}
