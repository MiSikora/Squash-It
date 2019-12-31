package io.mehow.squashit.report

internal data class IssueKey(
  val value: String
) {
  fun toIssueId() =
    IssueId(value.substringAfter("-").toLong())

  override fun toString() = value
}
