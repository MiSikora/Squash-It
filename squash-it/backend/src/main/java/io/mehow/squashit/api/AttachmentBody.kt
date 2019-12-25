package io.mehow.squashit.api

import io.mehow.squashit.Attachment
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.BufferedSink
import java.io.File

class AttachmentBody(
  val id: String,
  val part: MultipartBody.Part
) {
  override fun equals(other: Any?): Boolean {
    if (other !is AttachmentBody) return false
    if (id != other.id) return false
    return true
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun toString(): String {
    return "AttachmentBody(id='$id')"
  }

  companion object {
    fun fromFile(file: File): AttachmentBody {
      val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
      return AttachmentBody(file.name, part)
    }

    fun fromAttachment(attachment: Attachment): AttachmentBody {
      val part = MultipartBody.Part.createFormData(
          "file",
          attachment.name,
          attachment.asRequestBody()
      )
      return AttachmentBody(attachment.name, part)
    }

    private fun Attachment.asRequestBody(contentType: MediaType? = null): RequestBody {
      return object : RequestBody() {
        override fun contentType() = contentType

        override fun writeTo(sink: BufferedSink) {
          source()?.use { source -> sink.writeAll(source) }
        }
      }
    }
  }
}
