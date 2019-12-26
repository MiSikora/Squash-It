package io.mehow.squashit

import io.mehow.squashit.api.AttachmentBody

internal sealed class Report {
  internal abstract val description: Description?
  internal abstract val mentions: Mentions
  internal abstract val attachments: Set<AttachmentBody>

  data class NewIssue(
    override val description: Description?,
    override val mentions: Mentions,
    override val attachments: Set<AttachmentBody>,
    val reporter: User,
    val issueType: IssueType,
    val summary: Summary,
    val epic: Epic?
  ) : Report()

  data class AddComment(
    override val description: Description?,
    override val mentions: Mentions,
    override val attachments: Set<AttachmentBody>,
    val reporter: Reporter,
    val issueKey: IssueKey
  ) : Report()
}