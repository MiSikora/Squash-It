package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetUpdateIssueKey

internal class SetUpdateIssueKeyConsumer(
  sender: ModelSender
) : EventConsumer<SetUpdateIssueKey>(sender, SetUpdateIssueKey::class) {
  override suspend fun consume(event: SetUpdateIssueKey) {
    send { copy(updateIssueKey = event.key) }
  }
}
