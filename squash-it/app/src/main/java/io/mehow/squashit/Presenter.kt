package io.mehow.squashit

import io.mehow.squashit.UiModel.Accumulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class Presenter @Inject constructor(
  @Presentation private val context: CoroutineContext,
  private val store: CredentialsStore,
  promptDuration: Duration
) {
  private val uiModelsChannel = ConflatedBroadcastChannel<UiModel>()
  val uiModels get() = uiModelsChannel.asFlow()

  private val eventsChannel = Channel<Event>()
  suspend fun sendEvent(event: Event) {
    if (!eventsChannel.isClosedForSend) eventsChannel.send(event)
  }

  private val eventConsumers = setOf(
    UpsertCredentialsConsumer(store, promptDuration.value)::consume,
    DeleteCredentialsConsumer(store, promptDuration.value)::consume,
    DismissPromptConsumer::consume
  )

  private val presenterScope = CoroutineScope(SupervisorJob().apply {
    invokeOnCompletion {
      eventsChannel.cancel()
      uiModelsChannel.cancel()
    }
  })

  fun start() {
    presenterScope.launch(context) {
      val events = eventsChannel.consumeAsFlow().shareIn(this)
      (eventConsumers.map { it.invoke(events) } + credentialsAccumulator)
        .merge()
        .scan(UiModel(emptyList(), ActionState.Idle)) { model, (update) -> update(model) }
        .distinctUntilChanged()
        .onEach(uiModelsChannel::send)
        .launchIn(this)
    }
  }

  private val credentialsAccumulator
    get() = store
      .credentials
      .map { credentials -> Accumulator { it.copy(credentials = credentials) } }

  fun stop() {
    presenterScope.cancel()
  }
}
