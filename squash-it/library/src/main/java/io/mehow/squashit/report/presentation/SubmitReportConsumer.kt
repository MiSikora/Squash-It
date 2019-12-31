package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.CreateReportAttempt.Failure
import io.mehow.squashit.report.CreateReportAttempt.NoAttachments
import io.mehow.squashit.report.CreateReportAttempt.Success
import io.mehow.squashit.report.InputError
import io.mehow.squashit.report.JiraService
import io.mehow.squashit.report.Report
import io.mehow.squashit.report.Report.AddComment
import io.mehow.squashit.report.Report.NewIssue
import io.mehow.squashit.report.ReportAttempt
import io.mehow.squashit.report.ReportFactory
import io.mehow.squashit.report.SubmitState.Failed
import io.mehow.squashit.report.SubmitState.FailedToAttach
import io.mehow.squashit.report.SubmitState.Idle
import io.mehow.squashit.report.SubmitState.Submitted
import io.mehow.squashit.report.SubmitState.Submitting
import io.mehow.squashit.report.presentation.Event.SubmitReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

internal class SubmitReportConsumer(
  private val jiraService: JiraService
) : EventConsumer<SubmitReport> {
  override fun transform(events: Flow<SubmitReport>): Flow<Accumulator> {
    return events.transformLatest { event ->
      emit(Accumulator { copy(submitState = Submitting) })
      val accumulator = when (val reportAttempt = ReportFactory.create(event.input)) {
        is ReportAttempt.Valid -> sendReport(reportAttempt.report)
        is ReportAttempt.Invalid -> setInputErrors(reportAttempt.errors)
      }
      emit(accumulator)
    }
  }

  private suspend fun sendReport(report: Report): Accumulator {
    val attempt = when (report) {
      is NewIssue -> jiraService.createNewIssue(report)
      is AddComment -> jiraService.addComment(report)
    }
    val state = when (attempt) {
      is Success -> Submitted(attempt.key)
      is NoAttachments -> FailedToAttach(attempt.key, report.attachments)
      is Failure -> Failed(report)
    }
    return Accumulator { copy(submitState = state) }
  }

  private fun setInputErrors(errors: Set<InputError>): Accumulator {
    return Accumulator { copy(submitState = Idle, input = input.copy(errors = errors)) }
  }
}
