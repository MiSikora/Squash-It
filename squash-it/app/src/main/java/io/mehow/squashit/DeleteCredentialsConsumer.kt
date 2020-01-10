package io.mehow.squashit

import io.mehow.squashit.ActionState.Deleted
import io.mehow.squashit.ActionState.Idle
import io.mehow.squashit.Event.DeleteCredentials
import io.mehow.squashit.UiModel.Accumulator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Duration

class DeleteCredentialsConsumer(
  private val store: CredentialsStore,
  private val promptDuration: Duration
) : EventConsumer<DeleteCredentials> {
  override fun transform(events: Flow<DeleteCredentials>): Flow<Accumulator> {
    return events
        .mapNotNull { store.get(it.id) }
        .flatMapMerge { credentials ->
          store.delete(credentials.id)

          flow {
            val deleteState = Deleted(credentials)
            emit(Accumulator { currentModel -> currentModel.copy(state = deleteState) })

            delay(promptDuration.toLongMilliseconds())
            emit(Accumulator { currentModel ->
              val state = if (currentModel.state == deleteState) Idle else currentModel.state
              currentModel.copy(state = state)
            })
          }
        }
  }
}
