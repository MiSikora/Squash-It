package io.mehow.squashit.presentation

import io.mehow.squashit.SubmitState
import io.mehow.squashit.presentation.Event.GoIdle

internal class SetSubmitStateConsumer(
  sender: ModelSender
) : EventConsumer<GoIdle>(sender, GoIdle::class) {
  override suspend fun consume(event: GoIdle) {
    send { copy(submitState = SubmitState.Idle) }
  }
}
