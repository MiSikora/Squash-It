package io.mehow.squashit

internal data class NewIssue(
  val type: IssueType?,
  val summary: Summary?,
  val epic: Epic?
)
