package io.mehow.squashit.presentation

import io.mehow.squashit.IssueKey
import io.mehow.squashit.JiraService
import io.mehow.squashit.SubmitState
import io.mehow.squashit.SubmitState.FailedToAttachForComment
import io.mehow.squashit.SubmitState.RetryingAttachmentsForComment
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.api.Response.Failure
import io.mehow.squashit.api.Response.Success
import io.mehow.squashit.presentation.Event.RetryAttachmentsForComment
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class RetryAttachmentsForCommentConsumer(
  private val jiraService: JiraService,
  sender: ModelSender
) : EventConsumer<RetryAttachmentsForComment>(sender, RetryAttachmentsForComment::class) {
  private var currentJob: Job? = null

  override suspend fun consume(event: RetryAttachmentsForComment) = coroutineScope {
    currentJob?.cancel()
    currentJob = launch {
      send { copy(submitState = RetryingAttachmentsForComment) }
      send { addAttachments(event.key, event.attachments) }
    }
  }

  private suspend fun UiModel.addAttachments(
    key: IssueKey,
    attachments: Set<AttachmentBody>
  ): UiModel {
    val state = when (jiraService.addAttachments(key, attachments)) {
      is Success -> SubmitState.AddedAttachments(key)
      is Failure -> FailedToAttachForComment(key, attachments)
    }
    return copy(submitState = state)
  }
}
