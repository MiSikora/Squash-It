package io.mehow.squashit.report

import okio.Source

internal class Attachment(
  val id: AttachmentId,
  val name: String,
  val source: () -> Source?
) {
  override fun equals(other: Any?): Boolean {
    if (other !is Attachment) return false

    if (id != other.id) return false
    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }

  override fun toString(): String {
    return "AttachmentItem(id=$id, name='$name')"
  }
}
