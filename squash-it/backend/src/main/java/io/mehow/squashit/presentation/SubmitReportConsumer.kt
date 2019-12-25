package io.mehow.squashit.presentation

import io.mehow.squashit.CreateReportAttempt.Failure
import io.mehow.squashit.CreateReportAttempt.NoAttachments
import io.mehow.squashit.CreateReportAttempt.Success
import io.mehow.squashit.JiraService
import io.mehow.squashit.Report
import io.mehow.squashit.Report.AddComment
import io.mehow.squashit.Report.NewIssue
import io.mehow.squashit.ReportAttempt
import io.mehow.squashit.ReportFactory
import io.mehow.squashit.SubmitState.Failed
import io.mehow.squashit.SubmitState.FailedToAttach
import io.mehow.squashit.SubmitState.Idle
import io.mehow.squashit.SubmitState.Submitted
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
          is ReportAttempt.Valid -> sendReport(reportAttempt.report)
          is ReportAttempt.Invalid -> copy(submitState = Idle, inputErrors = reportAttempt.errors)
        }
      }
    }
  }

  private suspend fun UiModel.sendReport(report: Report): UiModel {
    val attempt = when (report) {
      is NewIssue -> jiraService.createNewIssue(report)
      is AddComment -> jiraService.addComment(report)
    }
    val state = when (attempt) {
      is Success -> Submitted(attempt.key)
      is NoAttachments -> FailedToAttach(attempt.key, report.attachments)
      is Failure -> Failed(report)
    }
    return copy(submitState = state)
  }
}
