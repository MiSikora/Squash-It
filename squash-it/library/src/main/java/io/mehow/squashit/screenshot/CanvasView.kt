package io.mehow.squashit.screenshot

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.graphics.Paint.Style.STROKE
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.os.bundleOf

internal class CanvasView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  private var paint = newPaint()
  private var brush: Brush? = null

  private var path = Path()
  private var pathHistory = mutableListOf<Path>()
  private var paintHistory = mutableListOf<Paint>()
  private var strokeHistory = mutableListOf<Stroke>()

  private var pathRedoHistory = mutableListOf<Path>()
  private var paintRedoHistory = mutableListOf<Paint>()
  private var strokeRedoHistory = mutableListOf<Stroke>()

  fun setBrush(brush: Brush) {
    this.brush = brush
    paint.color = brush.color
    paint.strokeWidth = brush.size.toFloat()
  }

  fun undo() {
    if (pathHistory.isNotEmpty()) {
      pathRedoHistory.add(pathHistory.removeLast())
      paintRedoHistory.add(paintHistory.removeLast())
      strokeRedoHistory.add(strokeHistory.removeLast())
      invalidate()
    }
  }

  fun redo() {
    if (pathRedoHistory.isNotEmpty()) {
      pathHistory.add(pathRedoHistory.removeLast())
      paintHistory.add(paintRedoHistory.removeLast())
      strokeHistory.add(strokeRedoHistory.removeLast())
      invalidate()
    }
  }

  fun clearCanvas() {
    pathRedoHistory.clear()
    paintRedoHistory.clear()
    strokeRedoHistory.clear()
    pathHistory.clear()
    paintHistory.clear()
    strokeHistory.clear()
    invalidate()
  }

  fun createAdjustedBitmap(width: Int, height: Int): Bitmap {
    val scale = height.toFloat() / this.height.toFloat()
    return createBitmap(width, height).applyCanvas {
      drawHistory(scale)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawHistory()
    canvas.drawPath(path, paint)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    val brush = brush ?: return false

    val x = event.x
    val y = event.y
    val touchHandled = when (event.action) {
      ACTION_DOWN -> startStroke(x, y, brush)
      ACTION_MOVE -> drawStroke(x, y)
      ACTION_UP -> endStroke(x, y, brush)
      else -> false
    }
    invalidate()
    return touchHandled
  }

  private fun startStroke(x: Float, y: Float, brush: Brush): Boolean {
    pathRedoHistory.clear()
    paintRedoHistory.clear()
    strokeRedoHistory.clear()
    strokeHistory.add(Stroke(mutableListOf(Dot(x, y)), brush))
    path.moveTo(x, y)
    return true
  }

  private fun drawStroke(x: Float, y: Float): Boolean {
    strokeHistory.last().dots.add(Dot(x, y))
    path.lineTo(x, y)
    return true
  }

  private fun endStroke(x: Float, y: Float, brush: Brush): Boolean {
    path.lineTo(x, y)
    strokeHistory.last().dots.add(Dot(x, y))
    pathHistory.add(path)
    paintHistory.add(paint)
    path = Path()
    paint = newPaint(brush)
    return true
  }

  private fun Canvas.drawHistory(scale: Float = 1f) {
    val scaleMatrix = Matrix().apply { setScale(scale, scale) }
    val scaledPaths = if (scale == 1f) pathHistory
    else pathHistory.map(::Path).onEach { it.transform(scaleMatrix) }
    for ((path, paint) in scaledPaths.zip(paintHistory)) {
      drawPath(path, paint)
    }
  }

  private fun newPaint(brush: Brush? = null): Paint {
    return Paint().apply {
      isAntiAlias = true
      style = STROKE
      strokeJoin = Join.ROUND
      strokeCap = Cap.ROUND
      if (brush != null) {
        color = brush.color
        strokeWidth = brush.size.toFloat()
      }
    }
  }

  private fun <T> MutableList<T>.removeLast() = removeAt(size - 1)

  override fun onRestoreInstanceState(state: Parcelable) {
    if (state is Bundle) {
      super.onRestoreInstanceState(state.getParcelable("instance"))
      brush = state.getParcelable("brush")!!
      strokeHistory = state.getParcelableArrayList("strokeHistory")!!
      pathHistory = strokeHistory.map(Stroke::path).toMutableList()
      paintHistory = strokeHistory.map { newPaint(it.brush) }.toMutableList()
      strokeRedoHistory = state.getParcelableArrayList("strokeRedoHistory")!!
      pathRedoHistory = strokeRedoHistory.map(Stroke::path).toMutableList()
      paintRedoHistory = strokeRedoHistory.map { newPaint(it.brush) }.toMutableList()
    } else super.onRestoreInstanceState(state)
  }

  override fun onSaveInstanceState(): Bundle {
    return bundleOf(
        "instance" to super.onSaveInstanceState(),
        "brush" to brush,
        "strokeHistory" to strokeHistory,
        "strokeRedoHistory" to strokeRedoHistory
    )
  }
}
