package io.mehow.squashit.report

import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.Report.AddComment
import io.mehow.squashit.report.api.AddCommentRequest
import io.mehow.squashit.report.api.JiraApi
import io.mehow.squashit.report.api.Response.Failure
import io.mehow.squashit.report.api.Response.Success

internal class AddCommentCall(
  private val report: AddComment,
  config: SquashItConfig,
) : ReportCall {
  private val key = report.issueKey
  private val request = AddCommentRequest(report.description(config))

  override suspend fun execute(jiraApi: JiraApi): CreateReportAttempt {
    return when (jiraApi.addComment(key, request)) {
      is Success -> if (report.attachments.isEmpty()) CreateReportAttempt.Success(key)
      else AddAttachmentsCall(key, report.attachments).execute(jiraApi)

      is Failure -> CreateReportAttempt.Failure
    }
  }
}
