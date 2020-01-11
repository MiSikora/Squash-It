package io.mehow.squashit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
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
import okio.source
import javax.inject.Inject

class ImportService : Service() {
  @Inject lateinit var database: Database
  @Inject lateinit var moshi: Moshi

  override fun onCreate() {
    AndroidInjection.inject(this)
  }

  @Suppress("LongMethod")
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val uri = intent.getParcelableExtra<Uri>(UriKey) ?: return START_NOT_STICKY

    val notifications = getSystemService<NotificationManager>()!!
    if (Build.VERSION.SDK_INT >= 26 && notifications.getNotificationChannel(ChannelId) == null) {
      val channelName = getString(R.string.credentials_import)
      val channel = NotificationChannel(ChannelId, channelName, IMPORTANCE_LOW)
      notifications.createNotificationChannel(channel)
    }

    startForeground(
        NotificationId,
        createNotification(
            R.string.import_service_title_importing,
            R.string.import_service_text_importing
        )
    )

    GlobalScope.launch {
      val moshiType = Types.newParameterizedType(List::class.java, JsonCredentials::class.java)
      val adapter = moshi.adapter<List<JsonCredentials>>(moshiType)

      val stream = contentResolver.openInputStream(uri)
      if (stream == null) {
        stopSelf(startId)
        notifications.notify(
            NotificationId,
            createNotification(
                R.string.import_service_title_failed,
                R.string.import_service_text_read_file_failure
            )
        )
        return@launch
      }

      val credentials = try {
        adapter.fromJson(stream.source().buffer())!!.map(JsonCredentials::asCredentials)
      } catch (_: Exception) {
        stopSelf(startId)
        notifications.notify(
            NotificationId,
            createNotification(
                R.string.import_service_title_failed,
                R.string.import_service_text_read_credentials_failure
            )
        )
        return@launch
      }

      with(database.credentialsQueries) {
        transaction {
          for (credential in credentials) {
            upsert(credential, transact = false)
          }
        }
      }

      withContext(Dispatchers.Main) {
        Toast.makeText(applicationContext, R.string.credentials_imported, LENGTH_SHORT).show()
      }
      stopSelf(startId)
    }

    return START_NOT_STICKY
  }

  private fun createNotification(@StringRes title: Int, @StringRes text: Int): Notification {
    return NotificationCompat.Builder(this, ChannelId)
        .setSmallIcon(R.drawable.ic_import)
        .setContentTitle(getString(title))
        .setContentText(getString(text))
        .setColor(ContextCompat.getColor(this, R.color.royal_blue_300))
        .build()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  companion object {
    private const val ChannelId = "credentials-importer"
    private const val NotificationId = 1
    private const val UriKey = "ImportService.UriKey"

    fun start(context: Context, uri: Uri) {
      val intent = Intent(context, ImportService::class.java)
      intent.putExtra(UriKey, uri)
      context.startService(intent)
    }
  }
}
