package io.mehow.squashit.report

import io.mehow.squashit.report.CreateReportAttempt.NoAttachments
import io.mehow.squashit.report.api.AttachmentBody
import io.mehow.squashit.report.api.JiraApi
import io.mehow.squashit.report.api.Response.Failure
import io.mehow.squashit.report.api.Response.Success

internal class AddAttachmentsCall(
  private val key: IssueKey,
  private val attachments: Set<AttachmentBody>
) : ReportCall {
  override suspend fun execute(jiraApi: JiraApi): CreateReportAttempt {
    val bodies = attachments.map(AttachmentBody::part)
    return when (jiraApi.attachFiles(key, bodies)) {
      is Success -> CreateReportAttempt.Success(key)
      is Failure -> NoAttachments(key)
    }
  }
}
