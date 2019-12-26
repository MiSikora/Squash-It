package io.mehow.squashit.presentation

import io.mehow.squashit.CreateReportAttempt.Failure
import io.mehow.squashit.CreateReportAttempt.NoAttachments
import io.mehow.squashit.CreateReportAttempt.Success
import io.mehow.squashit.JiraService
import io.mehow.squashit.Report
import io.mehow.squashit.Report.AddComment
import io.mehow.squashit.Report.NewIssue
import io.mehow.squashit.SubmitState.Failed
import io.mehow.squashit.SubmitState.FailedToAttach
import io.mehow.squashit.SubmitState.Resubmitting
import io.mehow.squashit.SubmitState.Submitted
import io.mehow.squashit.presentation.Event.RetrySubmission
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
