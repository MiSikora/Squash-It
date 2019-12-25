package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.HideError

internal class HideErrorConsumer(
  sender: ModelSender
) : EventConsumer<HideError>(sender, HideError::class) {
  override suspend fun consume(event: HideError) {
    send { copy(inputErrors = inputErrors - event.error) }
  }
}
