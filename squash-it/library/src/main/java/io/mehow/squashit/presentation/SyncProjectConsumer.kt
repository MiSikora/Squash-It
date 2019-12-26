package io.mehow.squashit.presentation

import io.mehow.squashit.InitState.Failure
import io.mehow.squashit.InitState.Idle
import io.mehow.squashit.InitState.Initializing
import io.mehow.squashit.JiraService
import io.mehow.squashit.presentation.Event.SyncProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

internal class SyncProjectConsumer(
  private val jiraService: JiraService
) : EventConsumer<SyncProject> {
  override fun transform(events: Flow<SyncProject>): Flow<Accumulator> {
    return events.transformLatest {
      emit(Accumulator { copy(initState = Initializing) })
      val info = jiraService.getProjectInfo()
      val accumulator = if (info == null) Accumulator { copy(initState = Failure) }
      else Accumulator { copy(initState = Idle, projectInfo = info) }
      emit(accumulator)
    }
  }
}
