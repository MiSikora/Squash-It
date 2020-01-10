package io.mehow.squashit

import io.mehow.squashit.UiModel.Accumulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class Presenter @Inject constructor(
  @Presentation private val context: CoroutineContext
) {
  private val uiModelsChannel = ConflatedBroadcastChannel<UiModel>()
  val uiModels get() = uiModelsChannel.asFlow()

  private val eventsChannel = Channel<Event>()
  suspend fun sendEvent(event: Event) {
    if (!eventsChannel.isClosedForSend) eventsChannel.send(event)
  }

  private val eventConsumers = emptySet<(Flow<Event>) -> Flow<Accumulator>>()

  private val presenterScope = CoroutineScope(SupervisorJob().apply {
    invokeOnCompletion {
      eventsChannel.cancel()
      uiModelsChannel.cancel()
    }
  })

  fun start() {
    presenterScope.launch(context) {
      val events = eventsChannel.consumeAsFlow().shareIn(this)
      eventConsumers.map { it.invoke(events) }
          .merge()
          .scan(UiModel(emptyList(), ActionState.Idle)) { model, (update) -> model.update() }
          .distinctUntilChanged()
          .onEach(uiModelsChannel::send)
          .launchIn(this)
    }
  }

  fun stop() {
    presenterScope.cancel()
  }
}
