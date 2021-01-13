package io.mehow.squashit.report.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance

internal interface EventConsumer<in EventT : Event> {
  fun transform(events: Flow<EventT>): Flow<Accumulator>
}

internal inline fun <reified T : Event> EventConsumer<T>.consume(
  events: Flow<Event>
): Flow<Accumulator> = transform(events.filterIsInstance())
