package io.mehow.squashit

import io.mehow.squashit.ActionState.Added
import io.mehow.squashit.ActionState.Idle
import io.mehow.squashit.ActionState.Updated
import io.mehow.squashit.Event.UpsertCredentials
import io.mehow.squashit.UiModel.Accumulator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

class UpsertCredentialsConsumer(
  private val store: CredentialsStore,
  private val promptDuration: Duration
) : EventConsumer<UpsertCredentials> {
  override fun transform(events: Flow<UpsertCredentials>): Flow<Accumulator> {
    return events
      .filter { (credentials, _) -> credentials.areValid() }
      .flatMapMerge { event ->
        val credentials = event.credentials
        val alreadyExists = store.get(credentials.id) != null
        store.upsert(credentials)

        if (!event.showPrompt) return@flatMapMerge emptyFlow()

        flow {
          val updateState = if (alreadyExists) Updated(credentials) else Added(credentials)
          emit(Accumulator { currentModel -> currentModel.copy(state = updateState) })

          delay(promptDuration.toLongMilliseconds())
          emit(Accumulator { currentModel ->
            val state = if (currentModel.state == updateState) Idle else currentModel.state
            currentModel.copy(state = state)
          })
        }
      }
  }

  private fun Credentials.areValid() = id.value.isNotBlank()
}
