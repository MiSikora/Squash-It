package io.mehow.squashit.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class IssueTypeResponse(
  val id: String,
  val name: String,
  @Json(name = "subtask") val isSubTask: Boolean
)
