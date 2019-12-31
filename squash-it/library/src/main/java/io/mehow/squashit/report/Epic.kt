package io.mehow.squashit.report

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Epic(
  val id: String,
  val name: String
)
