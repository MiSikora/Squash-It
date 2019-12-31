package io.mehow.squashit.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import io.mehow.squashit.report.SquashItConfig

internal class PaintboxCanvasCallback(
  private val canvasView: CanvasView,
  private val activity: Activity,
  private val screenshotBitmap: Bitmap
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
    val bitmap = createBitmap(screenshotBitmap.width, screenshotBitmap.height).applyCanvas {
      drawBitmap(screenshotBitmap, Matrix(), null)
      drawBitmap(canvasView.bitmap, Matrix(), null)
    }
    val screenshot = ScreenshotFactory.createScreenshotFile(activity, bitmap)
    SquashItConfig.create(activity).startActivity(activity, screenshot)
    activity.finish()
  }
}
