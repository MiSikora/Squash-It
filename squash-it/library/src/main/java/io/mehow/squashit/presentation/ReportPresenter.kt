package io.mehow.squashit.presentation

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mehow.squashit.AttachState.Attach
import io.mehow.squashit.JiraService
import io.mehow.squashit.ProjectInfoStore
import io.mehow.squashit.ServiceConfig
import io.mehow.squashit.api.EpicFieldsResponse
import io.mehow.squashit.api.JiraApi
import io.mehow.squashit.api.NewIssueFieldsRequest
import io.mehow.squashit.api.adapter.EpicFieldsResponseJsonAdapter
import io.mehow.squashit.presentation.Event.SyncProject
import io.mehow.squashit.presentation.Event.UpdateInput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import java.io.File
import java.lang.reflect.Type

internal class ReportPresenter internal constructor(
  jiraService: JiraService,
  private val createScreenshotFile: suspend () -> File?,
  private val createLogFile: suspend () -> File?
) {
  private val uiModelsChannel = ConflatedBroadcastChannel<UiModel>()

  private val eventsChannel = BroadcastChannel<Event>(1)
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
      eventConsumers.map { it.invoke(eventsChannel.asFlow()) }
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

  companion object {
    fun create(
      config: ServiceConfig,
      projectInfoDir: File,
      screenshotFileProvider: suspend () -> File?,
      logFileProvider: suspend () -> File?
    ): ReportPresenter {
      val moshi = Moshi.Builder()
          .add(EpicJsonFactory(config))
          .add(KotlinJsonAdapterFactory())
          .build()
      val projectInfoStore = ProjectInfoStore(projectInfoDir, moshi)
      val jiraApi = JiraApi.create(moshi, config)
      val jiraService = JiraService(config, projectInfoStore, jiraApi)
      return ReportPresenter(jiraService, screenshotFileProvider, logFileProvider)
    }
  }

  private class EpicJsonFactory(config: ServiceConfig) : JsonAdapter.Factory {
    private val readName = config.epicReadFieldName
    private val writeName = config.epicWriteFieldName
    override fun create(
      type: Type,
      annotations: MutableSet<out Annotation>,
      moshi: Moshi
    ): JsonAdapter<*>? {
      return when (type) {
        EpicFieldsResponse::class.java -> EpicFieldsResponseJsonAdapter(readName, moshi)
        NewIssueFieldsRequest::class.java -> EpicFieldsResponseJsonAdapter(writeName, moshi)
        else -> null
      }
    }
  }
}
