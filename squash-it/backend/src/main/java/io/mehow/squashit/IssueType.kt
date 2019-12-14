package io.mehow.squashit

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IssueType(
  val id: String,
  val name: String
)
