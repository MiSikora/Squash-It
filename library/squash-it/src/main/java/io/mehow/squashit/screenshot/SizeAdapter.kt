package io.mehow.squashit.screenshot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.squashit.R

internal class SizeAdapter(
  private val inflater: LayoutInflater,
  private val onSizeSelected: (Int) -> Unit,
) : ListAdapter<SizeItem, SizeViewHolder>(WidthItemCallback) {
  override fun getItemViewType(position: Int) = R.layout.squash_it_brush_size_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeViewHolder {
    val view = inflater.inflate(viewType, parent, false)
    return SizeViewHolder(view) { size ->
      onSizeSelected(size)
      submitList(currentList.map { it.copy(isSelected = it.size == size) })
    }
  }

  override fun onBindViewHolder(holder: SizeViewHolder, position: Int) {
    holder.bindTo(currentList[position])
  }

  private object WidthItemCallback : ItemCallback<SizeItem>() {
    override fun areItemsTheSame(old: SizeItem, new: SizeItem): Boolean {
      return old.size == new.size
    }

    override fun areContentsTheSame(old: SizeItem, new: SizeItem): Boolean {
      return old == new
    }

    override fun getChangePayload(old: SizeItem, new: SizeItem): Any? {
      return if (old == new) null else Unit // Disable item change animation.
    }
  }
}
