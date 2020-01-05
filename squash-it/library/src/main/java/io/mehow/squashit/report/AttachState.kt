package io.mehow.squashit.report

import io.mehow.squashit.report.api.AttachmentBody
import java.io.File

internal sealed class AttachState {
  abstract val file: File?
  abstract val body: AttachmentBody?

  data class Attach(override val file: File) : AttachState() {
    override val body: AttachmentBody? get() = AttachmentBody.fromFile(file)
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
