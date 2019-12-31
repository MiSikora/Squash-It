package io.mehow.squashit.report

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class User(
  val nameHandle: String,
  val accountId: String
) {
  val mentionTag = "[$nameHandle|~accountid:$accountId]"
}
