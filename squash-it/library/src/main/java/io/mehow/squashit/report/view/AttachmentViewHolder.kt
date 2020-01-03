package io.mehow.squashit.report.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.mehow.squashit.R
import io.mehow.squashit.report.Attachment
import io.mehow.squashit.report.AttachmentType
import io.mehow.squashit.report.AttachmentType.Image
import io.mehow.squashit.report.AttachmentType.Video

internal class AttachmentViewHolder(
  view: View,
  private val onRemoveAttachment: (Attachment) -> Unit
) : ViewHolder(view) {
  private val thumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)
  private val fileName = itemView.findViewById<TextView>(R.id.fileName)
  private val fileSize = itemView.findViewById<TextView>(R.id.fileSize)
  private val delete = itemView.findViewById<ImageView>(R.id.delete)

  private var item: Attachment? = null

  init {
    delete.setOnClickListener { onRemoveAttachment(item!!) }
  }

  fun bindTo(item: Attachment) {
    this.item = item
    thumbnail.setImageDrawable(item.type.getDrawable(itemView.context))
    fileName.text = item.name
    fileSize.text = item.size
  }

  private fun AttachmentType.getDrawable(context: Context): Drawable {
    val id = when (this) {
      Image -> R.drawable.ic_image
      Video -> R.drawable.ic_video
    }
    return AppCompatResources.getDrawable(context, id)!!
  }
}
