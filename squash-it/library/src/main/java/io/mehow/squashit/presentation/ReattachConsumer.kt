package io.mehow.squashit.presentation

import io.mehow.squashit.IssueKey
import io.mehow.squashit.JiraService
import io.mehow.squashit.SubmitState
import io.mehow.squashit.SubmitState.Reattaching
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.api.Response.Failure
import io.mehow.squashit.api.Response.Success
import io.mehow.squashit.presentation.Event.Reattach
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class ReattachConsumer(
  private val jiraService: JiraService,
  sender: ModelSender
) : EventConsumer<Reattach>(sender, Reattach::class) {
  private var currentJob: Job? = null

  override suspend fun consume(event: Reattach) = coroutineScope {
    currentJob?.cancel()
    currentJob = launch {
      send { copy(submitState = Reattaching) }
      send { addAttachments(event.key, event.attachments) }
    }
  }

  private suspend fun UiModel.addAttachments(
    key: IssueKey,
    attachments: Set<AttachmentBody>
  ): UiModel {
    return when (jiraService.addAttachments(key, attachments)) {
      is Success -> copy(submitState = SubmitState.AddedAttachments(key))
      is Failure -> copy(submitState = SubmitState.FailedToAttach(key, attachments))
    }
  }
}
