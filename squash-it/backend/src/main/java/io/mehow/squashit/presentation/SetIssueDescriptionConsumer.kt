package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetIssueDescription

internal class SetIssueDescriptionConsumer(
  sender: ModelSender
) : EventConsumer<SetIssueDescription>(sender, SetIssueDescription::class) {
  override suspend fun consume(event: SetIssueDescription) {
    send { copy(issueDescription = event.description) }
  }
}
