package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.Attachment
import io.mehow.squashit.report.AttachmentId
import io.mehow.squashit.report.Description
import io.mehow.squashit.report.Epic
import io.mehow.squashit.report.InputError
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.Mentions
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.User

internal data class UserInput(
  val reporter: User?,
  val reportType: ReportType,
  val type: IssueType?,
  val summary: Summary?,
  val epic: Epic?,
  val issueKey: IssueKey?,
  val description: Description?,
  val mentions: Mentions,
  val screenshotState: AttachState,
  val logsState: AttachState,
  val attachments: Set<Attachment>,
  val errors: Set<InputError>
) {
  fun withReporter(reporter: User): UserInput {
    return copy(reporter = reporter)
  }

  fun withReportType(type: ReportType): UserInput {
    return copy(reportType = type)
  }

  fun withIssueType(type: IssueType): UserInput {
    return copy(type = type)
  }

  fun withSummary(summary: Summary): UserInput {
    return copy(summary = summary)
  }

  fun withIssueEpic(epic: Epic): UserInput {
    return copy(epic = epic)
  }

  fun withIssueKey(key: IssueKey): UserInput {
    return copy(issueKey = key)
  }

  fun withDescription(description: Description): UserInput {
    return copy(description = description)
  }

  fun withMentions(vararg users: User): UserInput {
    return copy(mentions = mentions.withUsers(*users))
  }

  fun withoutMentions(vararg users: User): UserInput {
    return copy(mentions = mentions.withoutUsers(*users))
  }

  fun withScreenshot(state: AttachState): UserInput {
    return copy(screenshotState = state)
  }

  fun withLogs(state: AttachState): UserInput {
    return copy(logsState = state)
  }

  fun withAttachments(vararg attachments: Attachment): UserInput {
    return copy(attachments = this.attachments + attachments)
  }

  fun withoutAttachments(vararg ids: AttachmentId): UserInput {
    return copy(attachments = attachments - attachments.filter { it.id in ids })
  }

  fun withoutError(error: InputError): UserInput {
    return copy(errors = errors - error)
  }
}
