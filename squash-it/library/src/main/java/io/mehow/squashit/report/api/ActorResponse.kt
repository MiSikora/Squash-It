package io.mehow.squashit.report.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ActorResponse(
  val displayName: String,
  val actorUser: ActorUserResponse?
)
