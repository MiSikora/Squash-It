package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetIssueType

internal class SetNewIssueTypeConsumer(
  sender: ModelSender
) : EventConsumer<SetIssueType>(sender, SetIssueType::class) {
  override suspend fun consume(event: SetIssueType) {
    send { copy(newIssue = newIssue.copy(type = event.type)) }
  }
}
