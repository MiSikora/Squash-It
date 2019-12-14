package io.mehow.squashit.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.AttachState
import io.mehow.squashit.InitState
import io.mehow.squashit.presentation.Event.SyncProject
import org.junit.Test

class ReportPresenterInitTest : BaseReportPresenterTest() {
  @Test fun `available screenshot is reflected in UI state`() {
    val screenshot = folder.newFile()
    presenterFactory = presenterFactory.copy(screenshotFile = screenshot)

    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe idleModel.copy(screenshotState = AttachState.Attach(screenshot))

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test fun `available logs are reflected in UI state`() {
    val logs = folder.newFile()
    presenterFactory = presenterFactory.copy(logsFile = logs)

    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe idleModel.copy(logsState = AttachState.Attach(logs))

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test fun `data is synced on launch`() {
    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe syncedModel
    }
  }

  @Test fun `missing project data is handled gracefully`() {
    presenterFactory.jiraApi.projectFactory.enableErrors()

    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe idleModel.copy(initState = InitState.Failure)
    }
  }

  @Test fun `missing users data is handled gracefully`() {
    presenterFactory.jiraApi.roleFactory.enableErrors()

    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe idleModel.copy(initState = InitState.Failure)
    }
  }

  @Test fun `service errors do not matter after successful sync`() = testPresenter {
    presenterFactory.jiraApi.apply {
      projectFactory.enableErrors()
      roleFactory.enableErrors()
      epicsFactory.enableErrors()
    }

    sendEvent(SyncProject)
    expectItem() shouldBe syncedModel.copy(initState = InitState.Initializing)
    expectItem() shouldBe syncedModel
  }

  @Test fun `missing epics are not treated as a blocker`() {
    presenterFactory.jiraApi.epicsFactory.enableErrors()

    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe syncedModel.copy(
          projectInfo = syncedModel.projectInfo?.copy(epics = emptySet())
      )
    }
  }
}
