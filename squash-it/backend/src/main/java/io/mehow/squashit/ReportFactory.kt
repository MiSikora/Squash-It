package io.mehow.squashit

import io.mehow.squashit.InputError.NoIssueId
import io.mehow.squashit.InputError.NoIssueType
import io.mehow.squashit.InputError.NoReporter
import io.mehow.squashit.InputError.ShortSummary
import io.mehow.squashit.ReportType.CreateNewIssue
import io.mehow.squashit.ReportType.UpdateIssue
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.presentation.UiModel

internal object ReportFactory {
  fun create(model: UiModel) = when (model.reportType) {
    CreateNewIssue -> createNewReport(model)
    UpdateIssue -> createUpdateReport(model)
  }

  private fun createNewReport(model: UiModel): ReportAttempt {
    val inputErrors = model.newReportErrors
    return if (inputErrors.isEmpty()) ReportAttempt.Valid(model.asNewIssueReport())
    else ReportAttempt.Invalid(inputErrors)
  }

  private val UiModel.newReportErrors: Set<InputError>
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

  private fun UiModel.asNewIssueReport() = Report.NewIssue(
      description = issueDescription,
      mentions = mentions,
      attachments = allAttachments,
      reporter = reporter!!,
      issueType = newIssue.type!!,
      summary = newIssue.summary!!,
      epic = newIssue.epic
  )

  private fun createUpdateReport(model: UiModel): ReportAttempt {
    val inputErrors = model.addCommentErrors
    return if (inputErrors.isEmpty()) ReportAttempt.Valid(model.asAddCommentReport())
    else ReportAttempt.Invalid(inputErrors)
  }

  private val UiModel.addCommentErrors: Set<InputError>
    get() {
      val hasReporter = reporter != null
      val hasIssueId = updateIssueKey != null
      return listOfNotNull(
          if (!hasReporter) NoReporter else null,
          if (!hasIssueId) NoIssueId else null
      ).toSet()
    }

  private fun UiModel.asAddCommentReport() = Report.AddComment(
      description = issueDescription,
      mentions = mentions,
      attachments = allAttachments,
      reporter = Reporter(reporter!!),
      issueKey = updateIssueKey!!
  )

  private val UiModel.allAttachments: Set<AttachmentBody>
    get() {
      val list = listOfNotNull(
          screenshotState.file?.let { AttachmentBody.fromFile(it) },
          logsState.file?.let { AttachmentBody.fromFile(it) }
      ) + attachments.map { AttachmentBody.fromAttachment(it) }
      return list.toSet()
    }
}
