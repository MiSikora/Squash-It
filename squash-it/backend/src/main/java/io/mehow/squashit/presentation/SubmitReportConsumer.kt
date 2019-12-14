package io.mehow.squashit.presentation

import io.mehow.squashit.CreateReportAttempt
import io.mehow.squashit.CreateReportAttempt.Failure
import io.mehow.squashit.CreateReportAttempt.NoAttachments
import io.mehow.squashit.CreateReportAttempt.Success
import io.mehow.squashit.JiraService
import io.mehow.squashit.Report.AddComment
import io.mehow.squashit.Report.NewIssue
import io.mehow.squashit.ReportAttempt
import io.mehow.squashit.ReportFactory
import io.mehow.squashit.SubmitState.AddedComment
import io.mehow.squashit.SubmitState.CreatedNew
import io.mehow.squashit.SubmitState.Failed
import io.mehow.squashit.SubmitState.FailedToAttachForComment
import io.mehow.squashit.SubmitState.FailedToAttachForNew
import io.mehow.squashit.SubmitState.Idle
import io.mehow.squashit.SubmitState.Submitting
import io.mehow.squashit.presentation.Event.SubmitReport
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class SubmitReportConsumer(
  private val jiraService: JiraService,
  sender: ModelSender
) : EventConsumer<SubmitReport>(sender, SubmitReport::class) {
  private var currentJob: Job? = null

  override suspend fun consume(event: SubmitReport) = coroutineScope {
    currentJob?.cancel()
    currentJob = launch {
      send { copy(submitState = Submitting) }
      send {
        when (val reportAttempt = ReportFactory.create(this)) {
          is ReportAttempt.Valid -> sendReport(reportAttempt)
          is ReportAttempt.Invalid -> copy(
              submitState = Idle,
              inputErrors = reportAttempt.inputErrors
          )
        }
      }
    }
  }

  private suspend fun UiModel.sendReport(validAttempt: ReportAttempt.Valid): UiModel {
    return when (val report = validAttempt.report) {
      is NewIssue -> attachToNewIssue(jiraService.createNewIssue(report), report)
      is AddComment -> attachToComment(jiraService.addComment(report), report)
    }
  }

  private fun UiModel.attachToNewIssue(attempt: CreateReportAttempt, report: NewIssue): UiModel {
    val state = when (attempt) {
      is Success -> CreatedNew(attempt.key)
      is NoAttachments -> FailedToAttachForNew(attempt.key, report.attachments)
      Failure -> Failed(report)
    }
    return copy(submitState = state)
  }

  private fun UiModel.attachToComment(attempt: CreateReportAttempt, report: AddComment): UiModel {
    val state = when (attempt) {
      is Success -> AddedComment(report.issueKey)
      is NoAttachments -> FailedToAttachForComment(report.issueKey, report.attachments)
      Failure -> Failed(report)
    }
    return copy(submitState = state)
  }
}
