package io.mehow.squashit.api.extensions

import io.mehow.squashit.api.Response
import io.mehow.squashit.api.Response.Failure.Network
import io.mehow.squashit.api.Response.Success

internal fun <T : Any> T.asResponse(fail: Boolean): Response<T> {
  return if (fail) Network(
      RuntimeException("Expected failure.")
  )
  else Success(this)
}
