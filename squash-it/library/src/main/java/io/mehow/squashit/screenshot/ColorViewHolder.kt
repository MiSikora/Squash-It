package io.mehow.squashit.screenshot

import android.view.View
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.mehow.squashit.R

internal class ColorViewHolder(
  view: View,
  private val onColorSelected: (Int) -> Unit
) : ViewHolder(view) {
  private val colorSwatch = itemView.findViewById<View>(R.id.colorSwatch)
  private val select = itemView.findViewById<RadioButton>(R.id.select)

  private var colorItem: ColorItem? = null

  init {
    itemView.setOnClickListener { onColorSelected(colorItem!!.color) }
    select.isClickable = false
  }

  fun bindTo(item: ColorItem) {
    colorItem = item
    colorSwatch.setBackgroundColor(item.color)
    select.isChecked = item.isSelected
  }
}
