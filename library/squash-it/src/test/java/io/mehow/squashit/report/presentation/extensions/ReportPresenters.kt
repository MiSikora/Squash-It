package io.mehow.squashit.report.presentation.extensions

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.UiModel
import kotlinx.coroutines.test.TestCoroutineDispatcher

internal suspend fun ReportPresenter.test(
  dispatcher: TestCoroutineDispatcher,
  block: suspend FlowTurbine<UiModel>.() -> Unit,
) {
  uiModels.test {
    start(dispatcher)
    block()
    stop()
    expectComplete()
  }
}

