package io.mehow.squashit.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ProjectRequest(
  val key: String
)
