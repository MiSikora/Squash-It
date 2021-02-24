package io.mehow.squashit.screenshot

import android.view.View
import android.widget.FrameLayout.LayoutParams
import android.widget.RadioButton
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.mehow.squashit.R

internal class SizeViewHolder(
  view: View,
  private val onSizeSelected: (Int) -> Unit,
) : ViewHolder(view) {
  private val sizeSwatch = itemView.findViewById<View>(R.id.sizeSwatch)
  private val select = itemView.findViewById<RadioButton>(R.id.select)

  private var sizeItem: SizeItem? = null

  init {
    itemView.setOnClickListener { onSizeSelected(sizeItem!!.size) }
    select.isClickable = false
  }

  fun bindTo(item: SizeItem) {
    sizeItem = item
    sizeSwatch.updateLayoutParams<LayoutParams> { height = item.size }
    select.isChecked = item.isSelected
  }
}
