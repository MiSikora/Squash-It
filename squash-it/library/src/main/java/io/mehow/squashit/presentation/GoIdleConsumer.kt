package io.mehow.squashit.presentation

import io.mehow.squashit.SubmitState.Idle
import io.mehow.squashit.presentation.Event.GoIdle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal object GoIdleConsumer : EventConsumer<GoIdle> {
  override fun transform(events: Flow<GoIdle>): Flow<Accumulator> {
    return events.map { Accumulator { copy(submitState = Idle) } }
  }
}
