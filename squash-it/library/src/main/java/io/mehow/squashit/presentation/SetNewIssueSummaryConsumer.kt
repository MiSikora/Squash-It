package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetSummary

internal class SetNewIssueSummaryConsumer(
  sender: ModelSender
) : EventConsumer<SetSummary>(sender, SetSummary::class) {
  override suspend fun consume(event: SetSummary) {
    send { copy(input = input.copy(newIssue = input.newIssue.copy(summary = event.summary))) }
  }
}
