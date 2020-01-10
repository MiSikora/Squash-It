package io.mehow.squashit

import io.mehow.squashit.ActionState.Idle
import io.mehow.squashit.Event.DismissPrompt
import io.mehow.squashit.UiModel.Accumulator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DismissPromptConsumer : EventConsumer<DismissPrompt> {
  override fun transform(events: Flow<DismissPrompt>): Flow<Accumulator> {
    return events.map { Accumulator { it.copy(state = Idle) } }
  }
}
