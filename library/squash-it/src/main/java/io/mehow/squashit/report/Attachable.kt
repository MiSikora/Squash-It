package io.mehow.squashit.report

import io.mehow.squashit.report.api.AttachmentBody

internal interface Attachable {
  val body: AttachmentBody?
}
