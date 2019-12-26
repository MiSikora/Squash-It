package io.mehow.squashit.presentation

import io.mehow.squashit.AttachState
import io.mehow.squashit.Attachment
import io.mehow.squashit.Description
import io.mehow.squashit.Epic
import io.mehow.squashit.InputError
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.Mentions
import io.mehow.squashit.NewIssue
import io.mehow.squashit.ReportType
import io.mehow.squashit.Summary
import io.mehow.squashit.User

internal data class UserInput(
  val reporter: User?,
  val reportType: ReportType,
  val newIssue: NewIssue,
  val issueKey: IssueKey?,
  val description: Description?,
  val mentions: Mentions,
  val screenshotState: AttachState,
  val logsState: AttachState,
  val attachments: Set<Attachment>,
  val errors: Set<InputError>
) {
  private fun withNewIssue(builder: NewIssue.() -> NewIssue): UserInput {
    return copy(newIssue = newIssue.builder())
  }

  fun withNewIssueType(type: IssueType): UserInput {
    return withNewIssue { copy(type = type) }
  }

  fun withNewIssueSummary(summary: Summary): UserInput {
    return withNewIssue { copy(summary = summary) }
  }

  fun withNewIssueEpic(epic: Epic): UserInput {
    return withNewIssue { copy(epic = epic) }
  }

  fun withMentions(vararg users: User): UserInput {
    return copy(mentions = mentions.withUsers(*users))
  }

  fun withoutMentions(vararg users: User): UserInput {
    return copy(mentions = mentions.withoutUsers(*users))
  }

  fun withAttachments(vararg attachments: Attachment): UserInput {
    return copy(attachments = this.attachments + attachments)
  }

  fun withoutAttachments(vararg attachments: Attachment): UserInput {
    return copy(attachments = this.attachments - attachments)
  }

  fun withoutError(error: InputError): UserInput {
    return copy(errors = errors - error)
  }
}
