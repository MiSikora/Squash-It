package io.mehow.squashit.presentation

import io.mehow.squashit.IssueKey
import io.mehow.squashit.JiraService
import io.mehow.squashit.SubmitState
import io.mehow.squashit.SubmitState.Reattaching
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.api.Response.Failure
import io.mehow.squashit.api.Response.Success
import io.mehow.squashit.presentation.Event.Reattach
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
