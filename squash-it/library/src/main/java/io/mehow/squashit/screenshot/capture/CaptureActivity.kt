package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.core.content.getSystemService
import io.mehow.squashit.NoScreenshots

internal class CaptureActivity : Activity(), NoScreenshots {
  private var requestStartTime = 0L

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    val projectionManager = getSystemService<MediaProjectionManager>()!!
    val intent = projectionManager.createScreenCaptureIntent()
    requestStartTime = System.currentTimeMillis()
    startActivityForResult(intent, RequestCode)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val result = CaptureResult(resultCode, data, promptShown())
    CaptureRequest.sendBroadcast(this, result)
    finish()
  }

  private fun promptShown(): Boolean {
    return System.currentTimeMillis() - requestStartTime > 200
  }

  companion object {
    const val RequestCode = 200

    fun start(context: Context) {
      context.startActivity(Intent(context, CaptureActivity::class.java))
    }
  }
}
