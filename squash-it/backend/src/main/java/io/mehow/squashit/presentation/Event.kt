package io.mehow.squashit.presentation

import io.mehow.squashit.AttachState
import io.mehow.squashit.Attachment
import io.mehow.squashit.Description
import io.mehow.squashit.Epic
import io.mehow.squashit.InputError
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.Report
import io.mehow.squashit.ReportType
import io.mehow.squashit.Summary
import io.mehow.squashit.User
import io.mehow.squashit.api.AttachmentBody

sealed class Event {
  object SyncProject : Event()

  data class SetReporter(val user: User) : Event()

  data class SetReportType(val type: ReportType) : Event()

  data class SetNewIssueType(val type: IssueType) : Event()

  data class SetNewIssueSummary(val summary: Summary) : Event()

  data class SetNewIssueEpic(val epic: Epic) : Event()

  data class SetUpdateIssueKey(val key: IssueKey) : Event()

  data class SetIssueDescription(val description: Description) : Event()

  data class MentionUser(val user: User) : Event()

  data class UnmentionUser(val user: User) : Event()

  data class SetScreenshotState(val state: AttachState) : Event()

  data class SetLogsState(val state: AttachState) : Event()

  data class AddAttachment(val attachment: Attachment) : Event()

  data class RemoveAttachment(val attachment: Attachment) : Event()

  data class HideError(val inputError: InputError) : Event()

  object SubmitReport : Event()

  object GoIdle : Event()

  data class RetrySubmission(val report: Report) : Event()

  data class RetryAttachmentsForNew(
    val key: IssueKey,
    val attachments: Set<AttachmentBody>
  ) : Event()

  data class RetryAttachmentsForComment(
    val key: IssueKey,
    val attachments: Set<AttachmentBody>
  ) : Event()
}
