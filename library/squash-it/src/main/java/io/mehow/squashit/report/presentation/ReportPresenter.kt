package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.AttachState.Attach
import io.mehow.squashit.report.JiraService
import io.mehow.squashit.report.extensions.shareIn
import io.mehow.squashit.report.presentation.Event.SyncProject
import io.mehow.squashit.report.presentation.Event.UpdateInput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import java.io.File

internal class ReportPresenter internal constructor(
  jiraService: JiraService,
  private val createScreenshotFile: suspend () -> File?,
  private val createLogFile: suspend () -> File?,
) {
  private val uiModelsChannel = ConflatedBroadcastChannel<UiModel>()

  private val eventsChannel = Channel<Event>()
  private val eventConsumers = setOf(
      SyncProjectConsumer(jiraService)::consume,
      UpdateInputConsumer::consume,
      SubmitReportConsumer(jiraService)::consume,
      RetrySubmissionConsumer(jiraService)::consume,
      ReattachConsumer(jiraService)::consume,
      GoIdleConsumer::consume
  )

  private val presenterScope = CoroutineScope(SupervisorJob().apply {
    invokeOnCompletion {
      eventsChannel.cancel()
      uiModelsChannel.cancel()
    }
  })

  val uiModels get() = uiModelsChannel.asFlow()

  suspend fun sendEvent(event: Event) {
    if (!eventsChannel.isClosedForSend) eventsChannel.send(event)
  }

  fun start(dispatcher: CoroutineDispatcher) {
    presenterScope.launch(dispatcher) {
      val events = eventsChannel.consumeAsFlow().shareIn(this)
      eventConsumers.map { it.invoke(events) }
          .merge()
          .scan(UiModel.Initial) { model, (update) -> model.update() }
          .distinctUntilChanged()
          .onEach(uiModelsChannel::send)
          .launchIn(this)
      sendInitEvents()
    }
  }

  private fun CoroutineScope.sendInitEvents() = launch {
    createScreenshotFile()?.let { sendEvent(UpdateInput.screenshot(Attach(it))) }
    createLogFile()?.let { sendEvent(UpdateInput.logs(Attach(it))) }
    sendEvent(SyncProject)
  }

  fun stop() {
    presenterScope.cancel()
  }
}
