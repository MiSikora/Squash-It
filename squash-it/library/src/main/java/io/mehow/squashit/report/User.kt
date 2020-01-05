package io.mehow.squashit.report

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User internal constructor(val nameHandle: String, val accountId: String) {
  internal val mentionTag = "[$nameHandle|~accountid:$accountId]"
}
