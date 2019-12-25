package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetDescription

internal class SetIssueDescriptionConsumer(
  sender: ModelSender
) : EventConsumer<SetDescription>(sender, SetDescription::class) {
  override suspend fun consume(event: SetDescription) {
    send { copy(issueDescription = event.description) }
  }
}
