package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetEpic

internal class SetNewIssueEpicConsumer(
  sender: ModelSender
) : EventConsumer<SetEpic>(sender, SetEpic::class) {
  override suspend fun consume(event: SetEpic) {
    send { copy(newIssue = newIssue.copy(epic = event.epic)) }
  }
}
