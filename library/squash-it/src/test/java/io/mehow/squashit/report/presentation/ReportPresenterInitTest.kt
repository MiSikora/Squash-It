package io.mehow.squashit.report.presentation

import io.kotest.matchers.shouldBe
import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.InitState
import io.mehow.squashit.report.presentation.Event.SyncProject
import io.mehow.squashit.report.presentation.extensions.withInitState
import io.mehow.squashit.report.presentation.extensions.withLogs
import io.mehow.squashit.report.presentation.extensions.withProjectInfo
import io.mehow.squashit.report.presentation.extensions.withScreenshot
import org.junit.Test

internal class ReportPresenterInitTest : BaseReportPresenterTest() {
  @Test fun `available screenshot is reflected in UI state`() {
    val screenshot = folder.newFile()
    presenterFactory = presenterFactory.copy(screenshotFile = screenshot)

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe idleModel.withScreenshot(AttachState.Attach(screenshot))

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test fun `available logs are reflected in UI state`() {
    val logs = folder.newFile()
    presenterFactory = presenterFactory.copy(logsFile = logs)

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe idleModel.withLogs(AttachState.Attach(logs))

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test fun `data is synced on launch`() {
    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe syncedModel
    }
  }

  @Test fun `missing project data is handled gracefully`() {
    presenterFactory.jiraApi.projectFactory.enableErrors()

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe idleModel.withInitState(InitState.Failure)
    }
  }

  @Test fun `missing users data is handled gracefully`() {
    presenterFactory.jiraApi.roleFactory.enableErrors()

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe idleModel.withInitState(InitState.Failure)
    }
  }

  @Test fun `service errors do not matter after successful sync`() = testPresenter {
    presenterFactory.jiraApi.apply {
      projectFactory.enableErrors()
      roleFactory.enableErrors()
      epicsFactory.enableErrors()
    }

    presenter.sendEvent(SyncProject)
    expectItem() shouldBe syncedModel.withInitState(InitState.Initializing)
    expectItem() shouldBe syncedModel
  }

  @Test fun `missing epics are not treated as a blocker`() {
    presenterFactory.jiraApi.epicsFactory.enableErrors()

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe syncedModel.withProjectInfo { copy(epics = emptySet()) }
    }
  }
}
