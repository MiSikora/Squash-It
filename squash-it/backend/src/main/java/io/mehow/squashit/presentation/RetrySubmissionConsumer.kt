package io.mehow.squashit.presentation

import io.mehow.squashit.CreateReportAttempt
import io.mehow.squashit.CreateReportAttempt.Failure
import io.mehow.squashit.CreateReportAttempt.NoAttachments
import io.mehow.squashit.CreateReportAttempt.Success
import io.mehow.squashit.JiraService
import io.mehow.squashit.Report
import io.mehow.squashit.Report.AddComment
import io.mehow.squashit.Report.NewIssue
import io.mehow.squashit.SubmitState.AddedComment
import io.mehow.squashit.SubmitState.CreatedNew
import io.mehow.squashit.SubmitState.Failed
import io.mehow.squashit.SubmitState.FailedToAttachForComment
import io.mehow.squashit.SubmitState.FailedToAttachForNew
import io.mehow.squashit.SubmitState.RetryingSubmission
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
      send { copy(submitState = RetryingSubmission) }
      send { sendReport(event.report) }
    }
  }

  private suspend fun UiModel.sendReport(report: Report): UiModel {
    return when (report) {
      is NewIssue -> createNewIssue(jiraService.createNewIssue(report), report)
      is AddComment -> addComment(jiraService.addComment(report), report)
    }
  }

  private fun UiModel.createNewIssue(attempt: CreateReportAttempt, report: NewIssue): UiModel {
    val state = when (attempt) {
      is Success -> CreatedNew(attempt.key)
      is NoAttachments -> FailedToAttachForNew(attempt.key, report.attachments)
      is Failure -> Failed(report)
    }
    return copy(submitState = state)
  }

  private fun UiModel.addComment(attempt: CreateReportAttempt, report: AddComment): UiModel {
    val state = when (attempt) {
      is Success -> AddedComment(report.issueKey)
      is NoAttachments -> FailedToAttachForComment(report.issueKey, report.attachments)
      is Failure -> Failed(report)
    }
    return copy(submitState = state)
  }
}
