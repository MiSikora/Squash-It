package io.mehow.squashit.report.api.extensions

import io.mehow.squashit.report.api.Response
import io.mehow.squashit.report.api.Response.Failure.Network
import io.mehow.squashit.report.api.Response.Success

internal fun <T : Any> T.asResponse(fail: Boolean): Response<T> {
  return if (fail) Network(
      RuntimeException("Expected failure.")
  )
  else Success(this)
}
