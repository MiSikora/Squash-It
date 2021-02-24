package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.InitState.Failure
import io.mehow.squashit.report.InitState.Idle
import io.mehow.squashit.report.InitState.Initializing
import io.mehow.squashit.report.JiraService
import io.mehow.squashit.report.presentation.Event.SyncProject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

internal class SyncProjectConsumer(
  private val jiraService: JiraService,
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
