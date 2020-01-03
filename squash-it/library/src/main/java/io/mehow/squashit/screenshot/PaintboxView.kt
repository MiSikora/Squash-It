package io.mehow.squashit.screenshot

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import io.mehow.squashit.R

internal class PaintboxView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
  private val brushSizes = listOf(
      R.dimen.squash_it_brush_tiny,
      R.dimen.squash_it_brush_small,
      R.dimen.squash_it_brush_regular,
      R.dimen.squash_it_brush_large,
      R.dimen.squash_it_brush_huge
  ).map(resources::getDimensionPixelSize)

  private val brushColors = listOf(
      R.color.squash_it_fire_engine_red,
      R.color.squash_it_harlequin_green,
      R.color.squash_it_french_blue,
      R.color.squash_it_spiro_disco_ball,
      R.color.squash_it_dandelion,
      R.color.squash_it_flirt
  ).map { ContextCompat.getColor(context, it) }

  var enableSave = true
  var brush = Brush(brushSizes[2], brushColors[0])
    private set(value) {
      field = value
      callback?.onChangeBrush(value)
    }
  private var callback: Callback? = null

  fun setCallback(callback: Callback) {
    this.callback = callback
  }

  init {
    orientation = HORIZONTAL
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    findViewById<View>(R.id.clearCanvas).setOnClickListener { callback?.onClearCanvas() }
    val factory = BrushPartsFactory(context, brushColors, brushSizes)
    findViewById<View>(R.id.changeBrushColor).setOnClickListener {
      factory.showColorSelection(brush.color) { brush = brush.copy(color = it) }
    }
    findViewById<View>(R.id.changeBrushSize).setOnClickListener {
      factory.showWidthSelection(brush.size) { brush = brush.copy(size = it) }
    }
    findViewById<View>(R.id.undo).setOnClickListener { callback?.onUndo() }
    findViewById<View>(R.id.redo).setOnClickListener { callback?.onRedo() }
    findViewById<View>(R.id.save).setOnClickListener {
      if (!enableSave) return@setOnClickListener
      callback?.onSave()
    }
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    if (state is Bundle) {
      super.onRestoreInstanceState(state.getParcelable("instance"))
      brush = state.getParcelable("brush")!!
    } else super.onRestoreInstanceState(state)
  }

  override fun onSaveInstanceState(): Bundle {
    return bundleOf(
        "instance" to super.onSaveInstanceState(),
        "brush" to brush
    )
  }

  interface Callback {
    fun onClearCanvas()
    fun onChangeBrush(brush: Brush)
    fun onUndo()
    fun onRedo()
    fun onSave()
  }
}
