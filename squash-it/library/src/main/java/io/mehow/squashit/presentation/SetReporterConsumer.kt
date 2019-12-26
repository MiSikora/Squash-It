package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetReporter

internal class SetReporterConsumer(
  sender: ModelSender
) : EventConsumer<SetReporter>(sender, SetReporter::class) {
  override suspend fun consume(event: SetReporter) {
    send { copy(input = input.copy(reporter = event.reporter)) }
  }
}
