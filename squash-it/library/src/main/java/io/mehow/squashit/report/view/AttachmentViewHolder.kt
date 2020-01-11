package io.mehow.squashit.report.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES
import io.mehow.squashit.R
import io.mehow.squashit.report.AttachmentId
import io.mehow.squashit.report.AttachmentItem
import io.mehow.squashit.report.AttachmentType
import io.mehow.squashit.report.AttachmentType.Image
import io.mehow.squashit.report.AttachmentType.Video
import io.mehow.squashit.report.DiskSize

internal class AttachmentViewHolder(
  view: View,
  private val onRemoveAttachment: (AttachmentId) -> Unit
) : ViewHolder(view) {
  private val thumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)
  private val fileName = itemView.findViewById<TextView>(R.id.fileName)
  private val fileSize = itemView.findViewById<TextView>(R.id.fileSize)
  private val delete = itemView.findViewById<ImageView>(R.id.delete)

  private val textColor = itemView.context.run {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorOnSurface, typedValue, false)
    return@run ContextCompat.getColor(this, typedValue.data)
  }
  private val errorColor = itemView.context.run {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorError, typedValue, false)
    return@run ContextCompat.getColor(this, typedValue.data)
  }

  private var item: AttachmentItem? = null

  init {
    delete.setOnClickListener {
      onRemoveAttachment(item!!.id)
    }
  }

  fun bindTo(item: AttachmentItem) {
    this.item = item
    fileName.text = item.name
    bindSize(item.size)
    bindThumbnail(item)
  }

  private fun bindSize(size: DiskSize) {
    fileSize.text = size.displayValue
    if (size.value <= BytesWarningThreshold) fileSize.setTextColor(textColor)
    else fileSize.setTextColor(errorColor)
  }

  private fun bindThumbnail(item: AttachmentItem) {
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

  private companion object {
    val BytesWarningThreshold = MEGABYTES.toBytes(10)
  }
}
