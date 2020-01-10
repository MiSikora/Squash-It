package io.mehow.squashit

import io.mehow.squashit.ActionState.Deleted
import io.mehow.squashit.Event.DeleteCredentials
import io.mehow.squashit.UiModel.Accumulator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transform

class DeleteCredentialsConsumer(
  private val store: CredentialsStore
) : EventConsumer<DeleteCredentials> {
  override fun transform(events: Flow<DeleteCredentials>): Flow<Accumulator> {
    return events
        .mapNotNull { store.get(it.id) }
        .transform { credentials ->
          store.delete(credentials.id)
          emit(Accumulator { currentModel -> currentModel.copy(state = Deleted(credentials)) })
        }
  }
}
