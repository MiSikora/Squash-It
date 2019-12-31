package io.mehow.squashit.report

import java.io.File

internal sealed class AttachState {
  abstract val file: File?

  data class Attach(override val file: File) : AttachState()

  data class DoNotAttach(override val file: File) : AttachState()

  object Unavailable : AttachState() {
    @Suppress("ObjectPropertyNaming")
    override val file: File? = null
  }
}
