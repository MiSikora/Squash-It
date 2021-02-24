package io.mehow.squashit.report

import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.api.AttachmentBody

internal interface Report {
  val description: Description?
  val mentions: Mentions
  val attachments: Set<AttachmentBody>
  val reporter: User

  fun toCall(config: SquashItConfig): ReportCall

  fun description(config: SquashItConfig): String {
    return listOfNotNull(
        descriptionReporter(config),
        description,
        config.runtimeInfo,
        mentions
    ).joinToString("\n\n", transform = Describable::describe)
  }

  fun descriptionReporter(config: SquashItConfig): Reporter? {
    return if (config.allowReporterOverride) null else Reporter(reporter)
  }

  data class NewIssue(
    override val description: Description?,
    override val mentions: Mentions,
    override val attachments: Set<AttachmentBody>,
    override val reporter: User,
    val issueType: IssueType,
    val summary: Summary,
    val epic: Epic?,
  ) : Report {
    override fun toCall(config: SquashItConfig) = CreateIssueCall(this, config)
  }

  data class AddComment(
    override val description: Description?,
    override val mentions: Mentions,
    override val attachments: Set<AttachmentBody>,
    override val reporter: User,
    val issueKey: IssueKey,
  ) : Report {
    override fun toCall(config: SquashItConfig) = AddCommentCall(this, config)
    override fun descriptionReporter(config: SquashItConfig) = Reporter(reporter)
  }

  data class AddSubTask(
    override val description: Description?,
    override val mentions: Mentions,
    override val attachments: Set<AttachmentBody>,
    override val reporter: User,
    val summary: Summary,
    val parent: IssueKey,
  ) : Report {
    override fun toCall(config: SquashItConfig) = AddSubTaskCall(this, config)
  }
}
