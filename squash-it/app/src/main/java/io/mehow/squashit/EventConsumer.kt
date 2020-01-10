package io.mehow.squashit

import io.mehow.squashit.UiModel.Accumulator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance

interface EventConsumer<in EventT : Event> {
  fun transform(events: Flow<EventT>): Flow<Accumulator>
}

inline fun <reified T : Event> EventConsumer<T>.consume(
  events: Flow<Event>
): Flow<Accumulator> = transform(events.filterIsInstance())
