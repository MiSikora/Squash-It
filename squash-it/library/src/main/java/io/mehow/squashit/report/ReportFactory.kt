package io.mehow.squashit.report

import io.mehow.squashit.report.InputError.NoIssueId
import io.mehow.squashit.report.InputError.NoIssueType
import io.mehow.squashit.report.InputError.NoReporter
import io.mehow.squashit.report.InputError.ShortSummary
import io.mehow.squashit.report.ReportType.CreateNewIssue
import io.mehow.squashit.report.ReportType.UpdateIssue
import io.mehow.squashit.report.api.AttachmentBody
import io.mehow.squashit.report.presentation.UserInput

internal object ReportFactory {
  fun create(input: UserInput) = when (input.reportType) {
    CreateNewIssue -> createNewReport(input)
    UpdateIssue -> createUpdateReport(input)
  }

  private fun createNewReport(userInput: UserInput): ReportAttempt {
    val errors = userInput.newReportErrors
    return if (errors.isEmpty()) ReportAttempt.Valid(userInput.asNewIssueReport())
    else ReportAttempt.Invalid(errors)
  }

  private val UserInput.newReportErrors: Set<InputError>
    get() {
      val hasReporter = reporter != null
      val hasIssueType = newIssue.type != null
      val hasSummary = newIssue.summary?.value?.length ?: 0 >= 10
      return listOfNotNull(
          if (!hasReporter) NoReporter else null,
          if (!hasIssueType) NoIssueType else null,
          if (!hasSummary) ShortSummary else null
      ).toSet()
    }

  private fun UserInput.asNewIssueReport() = Report.NewIssue(
      description = description,
      mentions = mentions,
      attachments = allAttachments,
      reporter = reporter!!,
      issueType = newIssue.type!!,
      summary = newIssue.summary!!,
      epic = newIssue.epic
  )

  private fun createUpdateReport(userInput: UserInput): ReportAttempt {
    val errors = userInput.addCommentErrors
    return if (errors.isEmpty()) ReportAttempt.Valid(userInput.asAddCommentReport())
    else ReportAttempt.Invalid(errors)
  }

  private val UserInput.addCommentErrors: Set<InputError>
    get() {
      val hasReporter = reporter != null
      val hasIssueId = issueKey != null
      return listOfNotNull(
          if (!hasReporter) NoReporter else null,
          if (!hasIssueId) NoIssueId else null
      ).toSet()
    }

  private fun UserInput.asAddCommentReport() = Report.AddComment(
      description = description,
      mentions = mentions,
      attachments = allAttachments,
      reporter = reporter!!,
      issueKey = issueKey!!
  )

  private val UserInput.allAttachments: Set<AttachmentBody>
    get() {
      return (attachments + screenshotState + logsState)
          .mapNotNull(Attachable::body)
          .toSet()
    }
}
