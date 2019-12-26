package io.mehow.squashit.api

import io.mehow.squashit.api.extensions.asResponse

internal class UnitFactory {
  private var returnErrors = false

  fun enableErrors() {
    returnErrors = true
  }

  fun disableErrors() {
    returnErrors = false
  }

  fun create(): Response<Unit> {
    return Unit.asResponse(returnErrors)
  }
}
