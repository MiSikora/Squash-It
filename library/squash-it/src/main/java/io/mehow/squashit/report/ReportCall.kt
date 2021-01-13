package io.mehow.squashit.report

import io.mehow.squashit.report.api.JiraApi

internal interface ReportCall {
  suspend fun execute(jiraApi: JiraApi): CreateReportAttempt
}
