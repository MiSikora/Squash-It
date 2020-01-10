package io.mehow.squashit

import io.kotlintest.shouldBe
import io.mehow.squashit.ActionState.Added
import io.mehow.squashit.ActionState.Deleted
import io.mehow.squashit.ActionState.Idle
import io.mehow.squashit.ActionState.Updated
import io.mehow.squashit.Event.DeleteCredentials
import io.mehow.squashit.Event.UpsertCredentials
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.milliseconds

class PresenterTest {
  @get:Rule val database = JdbcDatabase()
  private val dispatcher = TestCoroutineDispatcher()
  private val store = CredentialsStore(database, dispatcher)
  private val presenter = Presenter(dispatcher, store, Duration(100.milliseconds))

  @Test fun `presentation starts with an idle state`() = test {
    presenter.uiModels.test {
      expectItem() shouldBe UiModel(emptyList(), Idle)

      cancel()
    }
  }

  @Test fun `inserting credentials displays it and prompts the user`() = test {
    presenter.uiModels.test {
      expectItem()

      val credentials = Credentials("ID", "Secret")
      presenter.sendEvent(UpsertCredentials(credentials))
      expectItem() shouldBe UiModel(listOf(credentials), Idle)
      expectItem() shouldBe UiModel(listOf(credentials), Added(credentials))

      dispatcher.advanceTimeBy(100)
      expectItem() shouldBe UiModel(listOf(credentials), Idle)

      cancel()
    }
  }

  @Test fun `blank credentials ID is ignored`() = test {
    presenter.uiModels.test {
      expectItem()

      val credentials = Credentials(" ", "Secret")
      presenter.sendEvent(UpsertCredentials(credentials))
      expectNoEvents()

      cancel()
    }
  }

  @Test fun `blank secret is not ignored`() = test {
    presenter.uiModels.test {
      expectItem()

      val credentials = Credentials("ID", " ")
      presenter.sendEvent(UpsertCredentials(credentials))
      expectItem() shouldBe UiModel(listOf(credentials), Idle)
      expectItem() shouldBe UiModel(listOf(credentials), Added(credentials))

      dispatcher.advanceTimeBy(100)
      expectItem() shouldBe UiModel(listOf(credentials), Idle)

      cancel()
    }
  }

  @Test fun `updating credentials displays it and prompts the user`() = test {
    store.upsert(Credentials("ID", "Secret"))

    presenter.uiModels.test {
      expectItem()

      val credentials = Credentials("ID", "Secret2")
      presenter.sendEvent(UpsertCredentials(credentials))
      expectItem() shouldBe UiModel(listOf(credentials), Idle)
      expectItem() shouldBe UiModel(listOf(credentials), Updated(credentials))

      dispatcher.advanceTimeBy(100)
      expectItem() shouldBe UiModel(listOf(credentials), Idle)

      cancel()
    }
  }

  @Test fun `new insert prompt is not dismissed due to previous insert action`() = test {
    presenter.uiModels.test {
      expectItem()

      val credentials1 = Credentials("ID 1", "Secret")
      presenter.sendEvent(UpsertCredentials(credentials1))
      expectItem() shouldBe UiModel(listOf(credentials1), Idle)
      expectItem() shouldBe UiModel(listOf(credentials1), Added(credentials1))

      dispatcher.advanceTimeBy(50)

      val credentials2 = Credentials("ID 2", "Secret")
      presenter.sendEvent(UpsertCredentials(credentials2))
      expectItem() shouldBe UiModel(listOf(credentials1, credentials2), Added(credentials1))
      expectItem() shouldBe UiModel(listOf(credentials1, credentials2), Added(credentials2))

      dispatcher.advanceTimeBy(50)
      expectNoEvents()

      dispatcher.advanceTimeBy(50)
      expectItem() shouldBe UiModel(listOf(credentials1, credentials2), Idle)

      cancel()
    }
  }

  @Test fun `new insert prompt is not dismissed due to previous update action`() = test {
    val credentials1 = Credentials("ID 1", "Secret 1")
    store.upsert(credentials1)

    presenter.uiModels.test {
      expectItem()

      val credentials2 = Credentials("ID 1", "Secret 2")
      presenter.sendEvent(UpsertCredentials(credentials2))
      expectItem() shouldBe UiModel(listOf(credentials2), Idle)
      expectItem() shouldBe UiModel(listOf(credentials2), Updated(credentials2))

      dispatcher.advanceTimeBy(50)

      val credentials3 = Credentials("ID 2", "Secret")
      presenter.sendEvent(UpsertCredentials(credentials3))
      expectItem() shouldBe UiModel(listOf(credentials2, credentials3), Updated(credentials2))
      expectItem() shouldBe UiModel(listOf(credentials2, credentials3), Added(credentials3))

      dispatcher.advanceTimeBy(50)
      expectNoEvents()

      dispatcher.advanceTimeBy(50)
      expectItem() shouldBe UiModel(listOf(credentials2, credentials3), Idle)

      cancel()
    }
  }

  @Test fun `new update prompt is not dismissed due to previous insert action`() = test {
    presenter.uiModels.test {
      expectItem()

      val credentials1 = Credentials("ID", "Secret 1")
      presenter.sendEvent(UpsertCredentials(credentials1))
      expectItem() shouldBe UiModel(listOf(credentials1), Idle)
      expectItem() shouldBe UiModel(listOf(credentials1), Added(credentials1))

      dispatcher.advanceTimeBy(50)

      val credentials2 = Credentials("ID", "Secret 2")
      presenter.sendEvent(UpsertCredentials(credentials2))
      expectItem() shouldBe UiModel(listOf(credentials2), Added(credentials1))
      expectItem() shouldBe UiModel(listOf(credentials2), Updated(credentials2))

      dispatcher.advanceTimeBy(50)
      expectNoEvents()

      dispatcher.advanceTimeBy(50)
      expectItem() shouldBe UiModel(listOf(credentials2), Idle)

      cancel()
    }
  }

  @Test fun `new update prompt is not dismissed due to previous update action`() = test {
    val credentials1 = Credentials("ID 1", "Secret 1")
    store.upsert(credentials1)
    val credentials2 = Credentials("ID 2", "Secret 1")
    store.upsert(credentials2)

    presenter.uiModels.test {
      expectItem()

      val credentials3 = Credentials("ID 1", "Secret 2")
      presenter.sendEvent(UpsertCredentials(credentials3))
      expectItem() shouldBe UiModel(listOf(credentials3, credentials2), Idle)
      expectItem() shouldBe UiModel(listOf(credentials3, credentials2), Updated(credentials3))

      dispatcher.advanceTimeBy(50)

      val credentials4 = Credentials("ID 2", "Secret 2")
      presenter.sendEvent(UpsertCredentials(credentials4))
      expectItem() shouldBe UiModel(listOf(credentials3, credentials4), Updated(credentials3))
      expectItem() shouldBe UiModel(listOf(credentials3, credentials4), Updated(credentials4))

      dispatcher.advanceTimeBy(50)
      expectNoEvents()

      dispatcher.advanceTimeBy(50)
      expectItem() shouldBe UiModel(listOf(credentials3, credentials4), Idle)

      cancel()
    }
  }

  @Test fun `prompt is not shown if not requested`() = test {
    presenter.uiModels.test {
      expectItem()

      val credentials = Credentials("ID", "Secret")
      presenter.sendEvent(UpsertCredentials(credentials, showPrompt = false))
      expectItem() shouldBe UiModel(listOf(credentials), Idle)

      cancel()
    }
  }

  @Test fun `credentials can be deleted`() = test {
    val credentials = Credentials("ID", "Secret")
    store.upsert(credentials)

    presenter.uiModels.test {
      expectItem()

      presenter.sendEvent(DeleteCredentials(credentials.id))
      expectItem() shouldBe UiModel(emptyList(), Idle)
      expectItem() shouldBe UiModel(emptyList(), Deleted(credentials))

      dispatcher.advanceTimeBy(100)
      expectItem() shouldBe UiModel(emptyList(), Idle)

      cancel()
    }
  }

  @Test fun `new delete prompt is not dismissed due to previous insert action`() = test {
    presenter.uiModels.test {
      expectItem()

      val credentials = Credentials("ID", "Secret")
      presenter.sendEvent(UpsertCredentials(credentials))
      expectItem() shouldBe UiModel(listOf(credentials), Idle)
      expectItem() shouldBe UiModel(listOf(credentials), Added(credentials))

      dispatcher.advanceTimeBy(50)

      presenter.sendEvent(DeleteCredentials(credentials.id))
      expectItem() shouldBe UiModel(emptyList(), Added(credentials))
      expectItem() shouldBe UiModel(emptyList(), Deleted(credentials))

      dispatcher.advanceTimeBy(50)
      expectNoEvents()

      dispatcher.advanceTimeBy(50)
      expectItem() shouldBe UiModel(emptyList(), Idle)

      cancel()
    }
  }

  @Test fun `new delete prompt is not dismissed due to previous update action`() = test {
    val credentials1 = Credentials("ID", "Secret 1")
    store.upsert(credentials1)

    presenter.uiModels.test {
      expectItem()

      val credentials2 = Credentials("ID", "Secret 2")
      presenter.sendEvent(UpsertCredentials(credentials2))
      expectItem() shouldBe UiModel(listOf(credentials2), Idle)
      expectItem() shouldBe UiModel(listOf(credentials2), Updated(credentials2))

      dispatcher.advanceTimeBy(50)

      presenter.sendEvent(DeleteCredentials(credentials1.id))
      expectItem() shouldBe UiModel(emptyList(), Updated(credentials2))
      expectItem() shouldBe UiModel(emptyList(), Deleted(credentials2))

      dispatcher.advanceTimeBy(50)
      expectNoEvents()

      dispatcher.advanceTimeBy(50)
      expectItem() shouldBe UiModel(emptyList(), Idle)

      cancel()
    }
  }

  private fun test(block: suspend TestCoroutineScope.() -> Unit) {
    dispatcher.runBlockingTest {
      presenter.start()
      block()
      presenter.stop()
    }
  }

  @Suppress("TestFunctionName")
  private fun Credentials(id: String, secret: String): Credentials {
    return Credentials.Impl(CredentialsId(id), Secret(secret))
  }
}
