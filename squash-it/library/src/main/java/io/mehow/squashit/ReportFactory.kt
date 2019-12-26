package io.mehow.squashit

import io.mehow.squashit.InputError.NoIssueId
import io.mehow.squashit.InputError.NoIssueType
import io.mehow.squashit.InputError.NoReporter
import io.mehow.squashit.InputError.ShortSummary
import io.mehow.squashit.ReportType.CreateNewIssue
import io.mehow.squashit.ReportType.UpdateIssue
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.presentation.UserInput

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
      reporter = Reporter(reporter!!),
      issueKey = issueKey!!
  )

  private val UserInput.allAttachments: Set<AttachmentBody>
    get() {
      val list = listOfNotNull(
          screenshotState.file?.let { AttachmentBody.fromFile(it) },
          logsState.file?.let { AttachmentBody.fromFile(it) }
      ) + attachments.map { AttachmentBody.fromAttachment(it) }
      return list.toSet()
    }
}
