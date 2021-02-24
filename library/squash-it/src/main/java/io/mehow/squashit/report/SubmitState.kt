package io.mehow.squashit.report

import io.mehow.squashit.report.api.AttachmentBody

internal sealed class SubmitState {
  object Idle : SubmitState()

  object Submitting : SubmitState()

  data class Submitted(val key: IssueKey) : SubmitState()

  data class Failed(val report: Report) : SubmitState()

  object Resubmitting : SubmitState()

  data class FailedToAttach(val key: IssueKey, val attachments: Set<AttachmentBody>) :
      SubmitState()

  object Reattaching : SubmitState()

  data class AddedAttachments(val key: IssueKey) : SubmitState()
}
