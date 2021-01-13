package io.mehow.squashit.report.api

import okhttp3.MultipartBody

internal class AttachmentBody(val id: String, val part: MultipartBody.Part) {
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
}
