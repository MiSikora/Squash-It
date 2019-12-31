package io.mehow.squashit.screenshot

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import io.mehow.squashit.report.SquashItConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class PaintboxCanvasCallback(
  private val canvasView: CanvasView,
  private val activity: ScreenshotActivity
) : PaintboxView.Callback {
  override fun onClearCanvas() {
    canvasView.clearCanvas()
  }

  override fun onChangeBrush(brush: Brush) {
    canvasView.setBrush(brush)
  }

  override fun onUndo() {
    canvasView.undo()
  }

  override fun onRedo() {
    canvasView.redo()
  }

  override fun onSave() {
    val screenshotBitmap = activity.screenshotBitmap
    val width = screenshotBitmap.width
    val height = screenshotBitmap.height
    activity.scope.launch {
      val canvasBitmap = canvasView.createAdjustedBitmap(width, height) ?: return@launch
      canvasView.isEnabled = false
      val screenshot = withContext(Dispatchers.IO) {
        createScreenshot(width, height, screenshotBitmap, canvasBitmap)
      }
      SquashItConfig.create(activity).startActivity(activity, screenshot)
      activity.finish()
      canvasView.isEnabled = true
    }
  }

  private suspend fun createScreenshot(
    width: Int,
    height: Int,
    screenshotBitmap: Bitmap,
    canvasBitmap: Bitmap
  ): File? {
    val bitmap = createBitmap(width, height).applyCanvas {
      drawBitmap(screenshotBitmap, Matrix(), null)
      drawBitmap(canvasBitmap, Matrix(), null)
    }
    return ScreenshotFactory.createScreenshotFile(activity, bitmap)
  }
}
