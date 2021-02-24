package io.mehow.squashit.report

import io.mehow.squashit.report.api.AttachmentBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Source

internal class Attachment(
  val id: AttachmentId,
  val name: String,
  private val source: () -> Source?,
) : Attachable {
  override val body: AttachmentBody
    get() {
      val part = MultipartBody.Part.createFormData("file", name, asRequestBody())
      return AttachmentBody(name, part)
    }

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

  private fun asRequestBody(contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {
      override fun contentType() = contentType

      override fun writeTo(sink: BufferedSink) {
        source()?.use { source -> sink.writeAll(source) }
      }
    }
  }
}
