package io.mehow.squashit.report.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.squashit.R
import io.mehow.squashit.report.AttachmentId
import io.mehow.squashit.report.AttachmentItem

internal class AttachmentAdapter(
  private val inflater: LayoutInflater,
  private val onRemoveAttachment: (AttachmentId) -> Unit
) : ListAdapter<AttachmentItem, AttachmentViewHolder>(AttachmentsItemCallback) {
  override fun getItemViewType(position: Int) = R.layout.squash_it_attachment_file_item

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
    val view = inflater.inflate(viewType, parent, false)
    return AttachmentViewHolder(view, onRemoveAttachment)
  }

  override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
    holder.bindTo(currentList[position])
  }

  private object AttachmentsItemCallback : ItemCallback<AttachmentItem>() {
    override fun areItemsTheSame(old: AttachmentItem, new: AttachmentItem): Boolean {
      return old.id == new.id
    }

    override fun areContentsTheSame(old: AttachmentItem, new: AttachmentItem): Boolean {
      return old == new
    }
  }
}
