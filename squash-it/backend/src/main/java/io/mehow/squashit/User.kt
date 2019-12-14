package io.mehow.squashit

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
  val nameHandle: String,
  val accountId: String
) {
  val mentionTag = "[$nameHandle|~accountid:$accountId]"
}
