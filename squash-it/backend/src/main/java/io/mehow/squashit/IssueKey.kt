package io.mehow.squashit

data class IssueKey(
  val value: String
) {
  fun toIssueId() = IssueId(value.substringAfter("-").toLong())

  override fun toString() = value
}
