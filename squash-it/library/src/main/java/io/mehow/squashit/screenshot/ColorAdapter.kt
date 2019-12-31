package io.mehow.squashit.screenshot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.squashit.R

internal class ColorAdapter(
  private val inflater: LayoutInflater,
  private val onColorSelected: (Int) -> Unit
) : ListAdapter<ColorItem, ColorViewHolder>(ColorItemCallback) {
  override fun getItemViewType(position: Int) = R.layout.brush_color_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
    val view = inflater.inflate(viewType, parent, false)
    return ColorViewHolder(view) { color ->
      onColorSelected(color)
      submitList(currentList.map { it.copy(isSelected = it.color == color) })
    }
  }

  override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
    holder.bindTo(currentList[position])
  }

  private object ColorItemCallback : ItemCallback<ColorItem>() {
    override fun areItemsTheSame(old: ColorItem, new: ColorItem): Boolean {
      return old.color == new.color
    }

    override fun areContentsTheSame(old: ColorItem, new: ColorItem): Boolean {
      return old == new
    }

    override fun getChangePayload(old: ColorItem, new: ColorItem): Any? {
      return if (old == new) null else Unit // Disable item change animation.
    }
  }
}
