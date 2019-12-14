package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetNewIssueEpic

internal class SetNewIssueEpicConsumer(
  sender: ModelSender
) : EventConsumer<SetNewIssueEpic>(sender, SetNewIssueEpic::class) {
  override suspend fun consume(event: SetNewIssueEpic) {
    send { copy(newIssue = newIssue.copy(epic = event.epic)) }
  }
}
