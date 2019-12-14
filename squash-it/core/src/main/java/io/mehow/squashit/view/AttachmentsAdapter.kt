package io.mehow.squashit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.squashit.AttachmentItem
import io.mehow.squashit.R

internal class AttachmentsAdapter(
  private val inflater: LayoutInflater,
  private val onRemoveItem: (AttachmentItem) -> Unit
) : ListAdapter<AttachmentItem, AttachmentsViewHolder>(AttachmentsItemCallback) {
  override fun getItemViewType(position: Int) = R.layout.attachment_file_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
    val view = inflater.inflate(viewType, parent, false)
    return AttachmentsViewHolder(view, onRemoveItem)
  }

  override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
    holder.bindTo(currentList[position])
  }

  private object AttachmentsItemCallback : ItemCallback<AttachmentItem>() {
    override fun areItemsTheSame(oldItem: AttachmentItem, newItem: AttachmentItem): Boolean {
      return newItem.name == oldItem.name
    }

    override fun areContentsTheSame(oldItem: AttachmentItem, newItem: AttachmentItem): Boolean {
      return oldItem == newItem
    }
  }
}
