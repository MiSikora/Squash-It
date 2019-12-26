package io.mehow.squashit.presentation

import io.mehow.squashit.InitState.Failure
import io.mehow.squashit.InitState.Idle
import io.mehow.squashit.InitState.Initializing
import io.mehow.squashit.JiraService
import io.mehow.squashit.presentation.Event.SyncProject
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class SyncProjectConsumer(
  private val jiraService: JiraService,
  sender: ModelSender
) : EventConsumer<SyncProject>(sender, SyncProject::class) {
  private var currentJob: Job? = null

  override suspend fun consume(event: SyncProject) {
    coroutineScope {
      currentJob?.cancel()
      currentJob = launch {
        send { copy(initState = Initializing) }
        val projectInfo = jiraService.getProjectInfo()
        if (projectInfo == null) send { copy(initState = Failure) }
        else send { copy(initState = Idle, projectInfo = projectInfo) }
      }
    }
  }
}
