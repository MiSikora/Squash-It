package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetNewIssueSummary

internal class SetNewIssueSummaryConsumer(
  sender: ModelSender
) : EventConsumer<SetNewIssueSummary>(sender, SetNewIssueSummary::class) {
  override suspend fun consume(event: SetNewIssueSummary) {
    send { copy(newIssue = newIssue.copy(summary = event.summary)) }
  }
}
