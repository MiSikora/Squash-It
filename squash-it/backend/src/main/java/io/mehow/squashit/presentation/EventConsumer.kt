package io.mehow.squashit.presentation

import io.mehow.squashit.JiraService
import kotlin.reflect.KClass

internal abstract class EventConsumer<EventT : Event>(
  private val sender: ModelSender,
  private val handledType: KClass<EventT>
) {
  suspend fun handle(event: Event) {
    if (event::class == handledType) consume(event as EventT)
  }

  abstract suspend fun consume(event: EventT)

  suspend fun send(builder: suspend UiModel.() -> UiModel) = sender.send(builder)

  companion object {
    @Suppress("LongMethod")
    fun createConsumers(service: JiraService, sender: ModelSender): List<suspend (Event) -> Unit> {
      return listOf(
          SyncProjectConsumer(service, sender)::handle,
          SetReporterConsumer(sender)::handle,
          SetReportTypeConsumer(sender)::handle,
          SetNewIssueTypeConsumer(sender)::handle,
          SetNewIssueEpicConsumer(sender)::handle,
          SetNewIssueSummaryConsumer(sender)::handle,
          SetUpdateIssueKeyConsumer(sender)::handle,
          SetIssueDescriptionConsumer(sender)::handle,
          MentionUserConsumer(sender)::handle,
          UnmentionUserConsumer(sender)::handle,
          SetScreenshotStateConsumer(sender)::handle,
          SetLogsStateConsumer(sender)::handle,
          AddAttachmentConsumer(sender)::handle,
          RemoveAttachmentConsumer(sender)::handle,
          HideErrorConsumer(sender)::handle,
          SubmitReportConsumer(service, sender)::handle,
          SetSubmitStateConsumer(sender)::handle,
          RetrySubmissionConsumer(service, sender)::handle,
          RetryAttachmentsForNewConsumer(service, sender)::handle,
          RetryAttachmentsForCommentConsumer(service, sender)::handle
      )
    }
  }
}
