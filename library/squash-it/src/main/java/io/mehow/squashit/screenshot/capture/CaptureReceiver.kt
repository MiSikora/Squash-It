package io.mehow.squashit.screenshot.capture

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.getSystemService

internal class CaptureReceiver private constructor(
  context: Context,
  private val takeScreenshot: (MediaProjection?) -> Unit,
  private val cancelScreenshot: () -> Unit,
) : BroadcastReceiver() {
  private val projectionManager = context.getSystemService<MediaProjectionManager>()!!
  private val handler = Handler(Looper.getMainLooper())

  override fun onReceive(context: Context, intent: Intent) {
    context.applicationContext.unregisterReceiver(this)

    val result = intent.getParcelableExtra<CaptureResult>(ExtraRequestResult)!!
    val data = result.data
    if (data == null) {
      cancelScreenshot()
      return
    }
    val projection = projectionManager.getMediaProjection(result.code, result.data)

    if (projection == null) {
      takeScreenshot(null)
      return
    }

    if (result.promptShown) handler.postDelayed({ takeScreenshot(projection) }, 500L)
    else takeScreenshot(projection)
  }

  companion object {
    private const val BroadcastAction = "io.mehow.squashit.screenshot.capture.action"
    private const val ExtraRequestResult = "io.mehow.squashit.screenshot.capture.extra"

    fun register(
      context: Context,
      captureScreenshot: (MediaProjection?) -> Unit,
      captureCancelled: () -> Unit,
    ) {
      val receiver = CaptureReceiver(context, captureScreenshot, captureCancelled)
      context.applicationContext.registerReceiver(receiver, IntentFilter(BroadcastAction))
    }

    fun sendBroadcast(context: Context, data: CaptureResult) {
      val intent = Intent(BroadcastAction).apply {
        putExtra(ExtraRequestResult, data)
      }
      context.sendBroadcast(intent)
    }
  }
}
