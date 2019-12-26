package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.UpdateInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal object UpdateInputConsumer : EventConsumer<UpdateInput> {
  override fun transform(events: Flow<UpdateInput>): Flow<Accumulator> {
    return events.map { (builder) -> Accumulator { copy(input = input.builder()) } }
  }
}
