package io.mehow.squashit

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class IssueType(
  val id: String,
  val name: String
)
