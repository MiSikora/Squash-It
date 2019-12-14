package io.mehow.squashit

import io.mehow.squashit.api.AttachmentBody

sealed class SubmitState {
  object Idle : SubmitState()

  object Submitting : SubmitState()

  data class CreatedNew(val key: IssueKey) : SubmitState()

  data class FailedToAttachForNew(
    val key: IssueKey,
    val attachments: Set<AttachmentBody>
  ) : SubmitState()

  object RetryingAttachmentsForNew : SubmitState()

  data class AddedComment(val key: IssueKey) : SubmitState()

  data class FailedToAttachForComment(
    val key: IssueKey,
    val attachments: Set<AttachmentBody>
  ) : SubmitState()

  object RetryingAttachmentsForComment : SubmitState()

  data class AddedAttachments(val key: IssueKey) : SubmitState()

  data class Failed(val report: Report) : SubmitState()

  object RetryingSubmission : SubmitState()
}
