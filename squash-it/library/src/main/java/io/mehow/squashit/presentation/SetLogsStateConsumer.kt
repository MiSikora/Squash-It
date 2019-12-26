package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetLogsState

internal class SetLogsStateConsumer(
  sender: ModelSender
) : EventConsumer<SetLogsState>(sender, SetLogsState::class) {
  override suspend fun consume(event: SetLogsState) {
    send { copy(input = input.copy(logsState = event.state)) }
  }
}
