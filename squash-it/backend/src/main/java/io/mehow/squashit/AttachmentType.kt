package io.mehow.squashit

enum class AttachmentType {
  Image,
  Video;

  companion object {
    fun fromMimeType(mimeType: String) = when {
      mimeType.contains("image", true) -> Image
      mimeType.contains("video", true) -> Video
      else -> null
    }
  }
}
