package io.mehow.squashit

import okio.Source

class Attachment(
  val type: AttachmentType,
  val name: String,
  val size: String,
  val source: () -> Source?
) {
  override fun equals(other: Any?): Boolean {
    if (other !is Attachment) return false

    if (type != other.type) return false
    if (name != other.name) return false
    if (size != other.size) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + size.hashCode()
    return result
  }

  override fun toString(): String {
    return "AttachmentItem(type=$type, name='$name', size='$size')"
  }
}
