package io.mehow.squashit.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.os.Handler
import android.os.Looper
import okio.IOException
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object ScreenshotFactory {
  private val FileNameFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'jpeg'", Locale.US)
  private val MainThreadHandler = Handler(Looper.getMainLooper())

  suspend fun createScreenshotFile(context: Context, bitmap: Bitmap): File? {
    return suspendCoroutine { continuation ->
      createScreenshotFile(context, bitmap) { continuation.resume(it) }
    }
  }

  @Suppress("LongMethod")
  private fun createScreenshotFile(context: Context, bitmap: Bitmap, onCreated: (File?) -> Unit) {
    fun sendScreenshotFile(file: File?) = MainThreadHandler.post {
      onCreated(file)
    }

    val dir = context.screenshotDirectory
    if (dir == null) {
      sendScreenshotFile(null)
      return
    }
    try {
      val output = File(dir, FileNameFormatter.format(Date()))
      output.sink().buffer().use { sink ->
        val bitmapStream = ByteArrayOutputStream()
        bitmap.compress(JPEG, 100, bitmapStream)
        sink.write(bitmapStream.toByteArray())
      }
      sendScreenshotFile(output)
    } catch (_: IOException) {
      sendScreenshotFile(null)
    }
  }

  fun cleanUp(context: Context) {
    val dir = context.screenshotDirectory ?: return
    for (file in dir.listFiles()!!) {
      if (file.extension == "jpeg") file.delete()
    }
  }

  @Suppress("ObjectPropertyNaming")
  private val Context.screenshotDirectory: File?
    get() {
      val externalDir = getExternalFilesDir(null) ?: return null
      val screenshotDir = File(externalDir, "/squash-it/screenshots")
      screenshotDir.mkdirs()
      return screenshotDir
    }
}
