package io.mehow.squashit.report

import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.Report.NewIssue
import io.mehow.squashit.report.api.IssueTypeRequest
import io.mehow.squashit.report.api.JiraApi
import io.mehow.squashit.report.api.NewIssueFieldsRequest
import io.mehow.squashit.report.api.NewIssueRequest
import io.mehow.squashit.report.api.ProjectRequest
import io.mehow.squashit.report.api.ReporterRequest
import io.mehow.squashit.report.api.Response.Failure
import io.mehow.squashit.report.api.Response.Success

internal class CreateIssueCall(
  private val report: NewIssue,
  config: SquashItConfig,
) : ReportCall {
  private val request = NewIssueRequest(
      NewIssueFieldsRequest(
          project = ProjectRequest(config.projectKey),
          parent = null,
          issueType = IssueTypeRequest(report.issueType.id),
          summary = report.summary.value,
          reporter = if (config.allowReporterOverride) ReporterRequest(report.reporter.accountId)
          else null,
          description = report.description(config),
          epic = report.epic?.id
      )
  )

  override suspend fun execute(jiraApi: JiraApi): CreateReportAttempt {
    return when (val createResponse = jiraApi.createNewIssue(request)) {
      is Success -> {
        val key = IssueKey(createResponse.value.key)
        if (report.attachments.isEmpty()) CreateReportAttempt.Success(key)
        else AddAttachmentsCall(key, report.attachments).execute(jiraApi)
      }
      is Failure -> CreateReportAttempt.Failure
    }
  }
}
