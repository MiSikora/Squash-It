package io.mehow.squashit.report.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.mehow.squashit.R
import io.mehow.squashit.report.AttachmentId
import io.mehow.squashit.report.AttachmentItem
import io.mehow.squashit.report.AttachmentType
import io.mehow.squashit.report.AttachmentType.Image
import io.mehow.squashit.report.AttachmentType.Video

internal class AttachmentViewHolder(
  view: View,
  private val onRemoveAttachment: (AttachmentId) -> Unit
) : ViewHolder(view) {
  private val thumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)
  private val fileName = itemView.findViewById<TextView>(R.id.fileName)
  private val fileSize = itemView.findViewById<TextView>(R.id.fileSize)
  private val delete = itemView.findViewById<ImageView>(R.id.delete)

  private var item: AttachmentItem? = null

  init {
    delete.setOnClickListener {
      onRemoveAttachment(item!!.id)
    }
  }

  fun bindTo(item: AttachmentItem) {
    this.item = item
    fileName.text = item.name
    fileSize.text = item.size
    val bitmap = item.thumbnail
    if (bitmap != null) thumbnail.setImageBitmap(bitmap)
    else thumbnail.setImageDrawable(item.type.getDrawable(itemView.context))
  }

  private fun AttachmentType.getDrawable(context: Context): Drawable {
    val id = when (this) {
      Image -> R.drawable.ic_image
      Video -> R.drawable.ic_video
    }
    return AppCompatResources.getDrawable(context, id)!!
  }
}
