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
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class RetrySubmissionConsumer(
  private val jiraService: JiraService,
  sender: ModelSender
) : EventConsumer<RetrySubmission>(sender, RetrySubmission::class) {
  private var currentJob: Job? = null

  override suspend fun consume(event: RetrySubmission) = coroutineScope {
    currentJob?.cancel()
    currentJob = launch {
      send { copy(submitState = Resubmitting) }
      send { sendReport(event.report) }
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
