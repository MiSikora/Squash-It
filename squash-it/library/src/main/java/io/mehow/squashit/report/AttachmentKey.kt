package io.mehow.squashit.report

import android.graphics.Point

internal data class AttachmentKey(val id: AttachmentId, val size: Int) {
  val point = Point(size, size)
}
