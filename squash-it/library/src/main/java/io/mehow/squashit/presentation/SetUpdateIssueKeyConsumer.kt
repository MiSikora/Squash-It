package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetIssueKey

internal class SetUpdateIssueKeyConsumer(
  sender: ModelSender
) : EventConsumer<SetIssueKey>(sender, SetIssueKey::class) {
  override suspend fun consume(event: SetIssueKey) {
    send { copy(input = input.copy(issueKey = event.key)) }
  }
}
