package io.mehow.squashit.report.presentation.extensions

import io.mehow.squashit.FlowAssert
import io.mehow.squashit.report.presentation.Event
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.UiModel
import io.mehow.squashit.test
import kotlinx.coroutines.test.TestCoroutineDispatcher

internal suspend fun ReportPresenter.test(
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

internal class PresenterAssert(
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
