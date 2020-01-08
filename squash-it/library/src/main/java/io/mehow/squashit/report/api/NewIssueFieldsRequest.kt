package io.mehow.squashit.report.api

internal data class NewIssueFieldsRequest(
  val project: ProjectRequest,
  val issueType: IssueTypeRequest,
  val summary: String,
  val reporter: ReporterRequest?,
  val description: String,
  val epic: String?
)
