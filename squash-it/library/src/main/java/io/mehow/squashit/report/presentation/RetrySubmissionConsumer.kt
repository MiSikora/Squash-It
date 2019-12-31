package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.CreateReportAttempt.Failure
import io.mehow.squashit.report.CreateReportAttempt.NoAttachments
import io.mehow.squashit.report.CreateReportAttempt.Success
import io.mehow.squashit.report.JiraService
import io.mehow.squashit.report.Report
import io.mehow.squashit.report.Report.AddComment
import io.mehow.squashit.report.Report.NewIssue
import io.mehow.squashit.report.SubmitState.Failed
import io.mehow.squashit.report.SubmitState.FailedToAttach
import io.mehow.squashit.report.SubmitState.Resubmitting
import io.mehow.squashit.report.SubmitState.Submitted
import io.mehow.squashit.report.presentation.Event.RetrySubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

internal class RetrySubmissionConsumer(
  private val jiraService: JiraService
) : EventConsumer<RetrySubmission> {
  override fun transform(events: Flow<RetrySubmission>): Flow<Accumulator> {
    return events.transformLatest { event ->
      emit(Accumulator { copy(submitState = Resubmitting) })
      emit(sendReport(event.report))
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
}
