package io.mehow.squashit.report

import android.graphics.Bitmap

internal data class AttachmentItem(
  val id: AttachmentId,
  val type: AttachmentType,
  val name: String,
  val size: DiskSize,
  val thumbnail: Bitmap?,
)
