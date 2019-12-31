package io.mehow.squashit.report

internal data class NewIssue(
  val type: IssueType?,
  val summary: Summary?,
  val epic: Epic?
)
