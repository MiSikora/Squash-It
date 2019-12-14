package io.mehow.squashit.presentation

import io.mehow.squashit.IssueKey
import io.mehow.squashit.JiraService
import io.mehow.squashit.SubmitState
import io.mehow.squashit.SubmitState.RetryingAttachmentsForNew
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.api.Response.Failure
import io.mehow.squashit.api.Response.Success
import io.mehow.squashit.presentation.Event.RetryAttachmentsForNew
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class RetryAttachmentsForNewConsumer(
  private val jiraService: JiraService,
  sender: ModelSender
) : EventConsumer<RetryAttachmentsForNew>(sender, RetryAttachmentsForNew::class) {
  private var currentJob: Job? = null

  override suspend fun consume(event: RetryAttachmentsForNew) = coroutineScope {
    currentJob?.cancel()
    currentJob = launch {
      send { copy(submitState = RetryingAttachmentsForNew) }
      send { addAttachments(event.key, event.attachments) }
    }
  }

  private suspend fun UiModel.addAttachments(
    key: IssueKey,
    attachments: Set<AttachmentBody>
  ): UiModel {
    return when (jiraService.addAttachments(key, attachments)) {
      is Success -> copy(submitState = SubmitState.AddedAttachments(key))
      is Failure -> copy(submitState = SubmitState.FailedToAttachForNew(key, attachments))
    }
  }
}
