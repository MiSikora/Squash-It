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

internal sealed class Event {
  object SyncProject : Event()

  data class SubmitReport(val input: UserInput) : Event()

  object GoIdle : Event()

  data class RetrySubmission(val report: Report) : Event()

  data class Reattach(val key: IssueKey, val attachments: Set<AttachmentBody>) : Event()

  data class UpdateInput(val builder: UserInput.() -> UserInput) : Event() {
    companion object {
      fun reporter(user: User) = UpdateInput { withReporter(user) }

      fun reportType(type: ReportType) = UpdateInput { withReportType(type) }

      fun issueType(type: IssueType) = UpdateInput { withNewIssueType(type) }

      fun summary(summary: Summary) = UpdateInput { withNewIssueSummary(summary) }

      fun epic(epic: Epic) = UpdateInput { withNewIssueEpic(epic) }

      fun issueKey(key: IssueKey) = UpdateInput { withIssueKey(key) }

      fun description(description: Description) = UpdateInput { withDescription(description) }

      fun mention(user: User) = UpdateInput { withMentions(user) }

      fun unmention(user: User) = UpdateInput { withoutMentions(user) }

      fun screenshot(state: AttachState) = UpdateInput { withScreenshot(state) }

      fun logs(state: AttachState) = UpdateInput { withLogs(state) }

      fun attach(attachment: Attachment) = UpdateInput { withAttachments(attachment) }

      fun detach(attachment: Attachment) = UpdateInput { withoutAttachments(attachment) }

      fun hideError(error: InputError) = UpdateInput { withoutError(error) }
    }
  }
}
