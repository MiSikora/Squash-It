package io.mehow.squashit.report

import io.mehow.squashit.report.api.AttachmentBody
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

internal sealed class AttachState : Attachable {
  abstract val file: File?

  data class Attach(override val file: File) : AttachState() {
    override val body: AttachmentBody?
      get() {
        val part =
          MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        return AttachmentBody(file.name, part)
      }
  }

  data class DoNotAttach(override val file: File) : AttachState() {
    override val body: AttachmentBody? = null
  }

  @Suppress("ObjectPropertyNaming")
  object Unavailable : AttachState() {
    override val file: File? = null
    override val body: AttachmentBody? = null
  }
}
