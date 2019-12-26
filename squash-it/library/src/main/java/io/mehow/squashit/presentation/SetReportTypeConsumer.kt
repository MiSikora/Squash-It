package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetReportType

internal class SetReportTypeConsumer(
  sender: ModelSender
) : EventConsumer<SetReportType>(sender, SetReportType::class) {
  override suspend fun consume(event: SetReportType) {
    send { copy(input = input.copy(reportType = event.type)) }
  }
}
