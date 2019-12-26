package io.mehow.squashit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.squashit.Attachment
import io.mehow.squashit.R

internal class AttachmentAdapter(
  private val inflater: LayoutInflater,
  private val onRemoveAttachment: (Attachment) -> Unit
) : ListAdapter<Attachment, AttachmentViewHolder>(AttachmentsItemCallback) {
  override fun getItemViewType(position: Int) = R.layout.attachment_file_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
    val view = inflater.inflate(viewType, parent, false)
    return AttachmentViewHolder(view, onRemoveAttachment)
  }

  override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
    holder.bindTo(currentList[position])
  }

  private object AttachmentsItemCallback : ItemCallback<Attachment>() {
    override fun areItemsTheSame(old: Attachment, new: Attachment): Boolean {
      return old.name == new.name
    }

    override fun areContentsTheSame(old: Attachment, new: Attachment): Boolean {
      return old == new
    }
  }
}
