package io.mehow.squashit.screenshot.capture

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.PixelFormat.RGBA_8888
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.core.content.getSystemService
import androidx.core.graphics.createBitmap
import io.mehow.squashit.screenshot.ScreenshotFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class ScreenshotProvider(
  private val activity: Activity,
  private val onScreenshot: (File?) -> Unit
) {
  @Volatile private var isCapturing = false
  private var useCanvasScreenshots = false
  private val screenshotTarget = activity.window.decorView
  private val windowManager = activity.getSystemService<WindowManager>()!!
  private val backgroundThread = HandlerThread("SquashIt", THREAD_PRIORITY_BACKGROUND).apply {
    start()
  }
  private val backgroundHandler = Handler(backgroundThread.looper)

  fun takeScreenshot() {
    if (!isSecureWindow() && !isCapturing) {
      isCapturing = true
      startCaptureReceiver()
      CaptureService.start(activity)
      CaptureActivity.start(activity)
    }
  }

  fun stopBackgroundThread() {
    backgroundThread.interrupt()
  }

  private fun isSecureWindow(): Boolean {
    return activity.window.attributes.flags and FLAG_SECURE != 0
  }

  private fun startCaptureReceiver() {
    CaptureReceiver.register(activity, ::takeScreenshot, ::cancelScreenshot)
  }

  private fun takeScreenshot(projection: MediaProjection?) {
    if (useCanvasScreenshots || projection == null) takeCanvasScreenshot()
    else takeNativeScreenshot(projection)
  }

  private fun cancelScreenshot() {
    CaptureService.stop(activity)
    isCapturing = false
  }

  private fun takeCanvasScreenshot() {
    screenshotTarget.isDrawingCacheEnabled = true
    val screenshot = Bitmap.createBitmap(screenshotTarget.drawingCache)
    screenshotTarget.isDrawingCacheEnabled = false
    saveScreenshot(screenshot)
  }

  @Suppress("LongMethod")
  private fun takeNativeScreenshot(projection: MediaProjection) {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getRealMetrics(displayMetrics)
    val width = displayMetrics.widthPixels
    val height = displayMetrics.heightPixels

    @SuppressLint("WrongConstant")
    val reader = ImageReader.newInstance(width, height, RGBA_8888, 2)
    val surface = reader.surface
    val display = projection.createVirtualDisplay(
      "SquashIt",
      width,
      height,
      displayMetrics.densityDpi,
      VIRTUAL_DISPLAY_FLAG_PRESENTATION,
      surface,
      null,
      null
    )

    reader.setOnImageAvailableListener({ reader ->
      var image: Image? = null
      var bitmap: Bitmap? = null

      try {
        val capturedImage = reader.acquireLatestImage().also { image = it }

        if (capturedImage == null) {
          useCanvasScreenshots = true
          takeCanvasScreenshot()
          return@setOnImageAvailableListener
        }
        val capturedBitmap = createPlaneBitmap(capturedImage, width, height).also { bitmap = it }
        val croppedBitmap = Bitmap.createBitmap(capturedBitmap, 0, 0, width, height)
        saveScreenshot(croppedBitmap)
      } catch (_: Exception) {
        useCanvasScreenshots = true
        takeCanvasScreenshot()
      } finally {
        bitmap?.recycle()
        image?.close()
        reader.close()
        display.release()
        projection.stop()
      }
    }, backgroundHandler)
  }

  private fun createPlaneBitmap(image: Image, width: Int, height: Int): Bitmap {
    val planes = image.planes
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * width

    return createBitmap(width + rowPadding / pixelStride, height).apply {
      copyPixelsFromBuffer(buffer)
    }
  }

  private fun saveScreenshot(bitmap: Bitmap) {
    GlobalScope.launch(Dispatchers.Main) {
      val file = withContext(Dispatchers.IO) {
        ScreenshotFactory.createScreenshotFile(activity, bitmap)
      }
      CaptureService.stop(activity)
      onScreenshot(file)
      isCapturing = false
    }
  }
}
