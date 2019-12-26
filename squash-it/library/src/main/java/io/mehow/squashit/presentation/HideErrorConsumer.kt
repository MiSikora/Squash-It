package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.DismissError

internal class HideErrorConsumer(
  sender: ModelSender
) : EventConsumer<DismissError>(sender, DismissError::class) {
  override suspend fun consume(event: DismissError) {
    send { copy(errors = errors - event.error) }
  }
}
