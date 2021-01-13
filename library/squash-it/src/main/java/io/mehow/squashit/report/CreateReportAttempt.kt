package io.mehow.squashit.report

internal sealed class CreateReportAttempt {
  data class Success(val key: IssueKey) : CreateReportAttempt()
  data class NoAttachments(val key: IssueKey) : CreateReportAttempt()
  object Failure : CreateReportAttempt()
}
