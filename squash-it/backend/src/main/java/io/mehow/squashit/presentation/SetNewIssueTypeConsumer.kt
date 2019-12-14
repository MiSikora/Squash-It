package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetNewIssueType

internal class SetNewIssueTypeConsumer(
  sender: ModelSender
) : EventConsumer<SetNewIssueType>(sender, SetNewIssueType::class) {
  override suspend fun consume(event: SetNewIssueType) {
    send { copy(newIssue = newIssue.copy(type = event.type)) }
  }
}
