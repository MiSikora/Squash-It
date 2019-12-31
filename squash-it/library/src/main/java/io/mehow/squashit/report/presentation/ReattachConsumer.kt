package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.JiraService
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.SubmitState.Reattaching
import io.mehow.squashit.report.api.AttachmentBody
import io.mehow.squashit.report.api.Response.Failure
import io.mehow.squashit.report.api.Response.Success
import io.mehow.squashit.report.presentation.Event.Reattach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

internal class ReattachConsumer(
  private val jiraService: JiraService
) : EventConsumer<Reattach> {
  override fun transform(events: Flow<Reattach>): Flow<Accumulator> {
    return events.transformLatest { event ->
      emit(Accumulator { copy(submitState = Reattaching) })
      emit(addAttachments(event.key, event.attachments))
    }
  }

  private suspend fun addAttachments(key: IssueKey, attachments: Set<AttachmentBody>): Accumulator {
    return when (jiraService.addAttachments(key, attachments)) {
      is Success -> Accumulator { copy(submitState = SubmitState.AddedAttachments(key)) }
      is Failure -> Accumulator { copy(submitState = SubmitState.FailedToAttach(key, attachments)) }
    }
  }
}
