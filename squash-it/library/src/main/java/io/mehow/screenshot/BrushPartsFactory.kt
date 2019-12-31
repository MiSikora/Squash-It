package io.mehow.screenshot

import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.mehow.squashit.R
import io.mehow.squashit.extensions.layoutInflater

internal class BrushPartsFactory(
  private val context: Context,
  private val colors: List<Int>,
  private val sizes: List<Int>
) {
  @ColorInt private var colorChoice: Int = 0
  private val colorAdapter = ColorAdapter(context.layoutInflater) { colorChoice = it }

  fun showColorSelection(@ColorInt selectedColor: Int, onColorSelected: (Int) -> Unit) {
    colorAdapter.submitList(colors.map { ColorItem(it, it == selectedColor) })
    val colorRecycler = RecyclerView(context).apply {
      layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      layoutManager = LinearLayoutManager(context)
      adapter = colorAdapter
    }
    MaterialAlertDialogBuilder(context)
        .setView(colorRecycler)
        .setTitle(R.string.squash_it_select_brush_color)
        .setNegativeButton(R.string.squash_it_cancel, null)
        .setPositiveButton(R.string.squash_it_select) { _, _ -> onColorSelected(colorChoice) }
        .create()
        .show()
  }

  @Px private var sizeChoice: Int = 0
  private val sizeAdapter = SizeAdapter(context.layoutInflater) { sizeChoice = it }

  fun showWidthSelection(@Px selectedWidth: Int, onWidthSelected: (Int) -> Unit) {
    sizeAdapter.submitList(sizes.map { SizeItem(it, it == selectedWidth) })
    val sizeRecycler = RecyclerView(context).apply {
      layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      layoutManager = LinearLayoutManager(context)
      adapter = sizeAdapter
    }
    MaterialAlertDialogBuilder(context)
        .setView(sizeRecycler)
        .setTitle(R.string.squash_it_select_brush_size)
        .setNegativeButton(R.string.squash_it_cancel, null)
        .setPositiveButton(R.string.squash_it_select) { _, _ -> onWidthSelected(sizeChoice) }
        .create()
        .show()
  }
}
