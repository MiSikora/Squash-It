package io.mehow.squashit.report.presentation.extensions

import io.mehow.squashit.FlowAssert
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.UiModel
import io.mehow.squashit.test
import kotlinx.coroutines.test.TestCoroutineDispatcher

internal suspend fun ReportPresenter.test(
  dispatcher: TestCoroutineDispatcher,
  block: suspend FlowAssert<UiModel>.() -> Unit
) {
  uiModels.test {
    start(dispatcher)
    block()
    stop()
    expectComplete()
  }
}

