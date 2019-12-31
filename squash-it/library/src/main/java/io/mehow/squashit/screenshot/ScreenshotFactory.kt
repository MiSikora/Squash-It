package io.mehow.squashit.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import okio.IOException
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object ScreenshotFactory {
  private val fileNameFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'jpeg'", Locale.US)

  fun createScreenshotFile(context: Context, bitmap: Bitmap): File? {
    val dir = context.screenshotDirectory ?: return null
    return try {
      val output = File(dir, fileNameFormatter.format(Date()))
      output.sink().buffer().use { sink ->
        val bitmapStream = ByteArrayOutputStream()
        bitmap.compress(JPEG, 100, bitmapStream)
        sink.write(bitmapStream.toByteArray())
      }
      output
    } catch (_: IOException) {
      null
    }
  }

  fun cleanUp(context: Context) {
    val dir = context.screenshotDirectory ?: return
    for (file in dir.listFiles()!!) {
      if (file.extension == "jpeg") file.delete()
    }
  }

  private val Context.screenshotDirectory: File?
    get() {
      val externalDir = getExternalFilesDir(null) ?: return null
      val logDir = File(externalDir, "/squash-it/screenshots")
      logDir.mkdirs()
      return logDir
    }
}
