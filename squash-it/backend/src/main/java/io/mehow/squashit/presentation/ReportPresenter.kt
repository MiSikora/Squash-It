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
import io.mehow.squashit.presentation.Event.SetLogsState
import io.mehow.squashit.presentation.Event.SetScreenshotState
import io.mehow.squashit.presentation.Event.SyncProject
import io.mehow.squashit.presentation.UiModel.Companion.Initial
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import java.io.File
import java.lang.reflect.Type

class ReportPresenter internal constructor(
  jiraService: JiraService,
  private val createScreenshotFile: suspend () -> File?,
  private val createLogFile: suspend () -> File?
) {
  private val uiModelsChannel = ConflatedBroadcastChannel<UiModel>()
  private val modelSender = ModelSender(Initial) {
    if (!uiModelsChannel.isClosedForSend) uiModelsChannel.send(it)
  }

  private val eventsChannel = Channel<Event>()
  private val eventConsumers = EventConsumer.createConsumers(jiraService, modelSender)

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
      launch {
        createScreenshotFile()?.let { sendEvent(SetScreenshotState(Attach(it))) }
        createLogFile()?.let { sendEvent(SetLogsState(Attach(it))) }
        sendEvent(SyncProject)
      }
      eventsChannel.consumeEach { event ->
        for (consumeEvent in eventConsumers) consumeEvent(event)
      }
    }
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
