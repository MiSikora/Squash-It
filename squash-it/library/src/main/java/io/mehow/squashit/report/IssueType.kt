package io.mehow.squashit.report

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class IssueType(
  val id: String,
  val name: String
)
