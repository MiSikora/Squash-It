package io.mehow.squashit.presentation.extensions

import io.mehow.squashit.FlowAssert
import io.mehow.squashit.presentation.Event
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import io.mehow.squashit.test
import kotlinx.coroutines.test.TestCoroutineDispatcher

suspend fun ReportPresenter.test(
  dispatcher: TestCoroutineDispatcher,
  block: suspend PresenterAssert.() -> Unit
) {
  uiModels.test {
    val presenterAssert = PresenterAssert(this@test, this)
    start(dispatcher)
    presenterAssert.block()
    stop()
    expectComplete()
  }
}

class PresenterAssert(
  val presenter: ReportPresenter,
  private val flowAssert: FlowAssert<UiModel>
) {
  suspend fun sendEvent(event: Event) = presenter.sendEvent(event)

  fun cancel() = flowAssert.cancel()
  fun cancelAndIgnoreRemainingEvents(): Nothing = flowAssert.cancelAndIgnoreRemainingEvents()
  fun expectNoEvents() = flowAssert.expectNoEvents()
  suspend fun expectNoMoreEvents() = flowAssert.expectNoMoreEvents()
  suspend fun expectItem(): UiModel = flowAssert.expectItem()
  suspend fun expectComplete() = flowAssert.expectComplete()
  suspend fun expectError() = flowAssert.expectError()
}
