package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.SubmitState.Idle
import io.mehow.squashit.report.presentation.Event.GoIdle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal object GoIdleConsumer : EventConsumer<GoIdle> {
  override fun transform(events: Flow<GoIdle>): Flow<Accumulator> {
    return events.map { Accumulator { copy(submitState = Idle) } }
  }
}
