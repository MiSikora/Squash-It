package io.mehow.squashit

import io.kotlintest.shouldBe
import io.mehow.squashit.ActionState.Added
import io.mehow.squashit.ActionState.Idle
import io.mehow.squashit.ActionState.Updated
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

      val credentials = Credentials("ID", "Token")
      presenter.sendEvent(UpsertCredentials(credentials))
      expectItem() shouldBe UiModel(listOf(credentials), Idle)
      expectItem() shouldBe UiModel(listOf(credentials), Added(credentials))

      dispatcher.advanceTimeBy(100)
      expectItem() shouldBe UiModel(listOf(credentials), Idle)

      cancel()
    }
  }

  @Test fun `updating credentials displays it and prompts the user`() = test {
    store.upsert(Credentials("ID", "Token"))

    presenter.uiModels.test {
      expectItem()

      val credentials = Credentials("ID", "Token2")
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

      val credentials1 = Credentials("ID 1", "Token")
      presenter.sendEvent(UpsertCredentials(credentials1))
      expectItem() shouldBe UiModel(listOf(credentials1), Idle)
      expectItem() shouldBe UiModel(listOf(credentials1), Added(credentials1))

      dispatcher.advanceTimeBy(50)

      val credentials2 = Credentials("ID 2", "Token")
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
    val credentials1 = Credentials("ID 1", "Token 1")
    store.upsert(credentials1)

    presenter.uiModels.test {
      expectItem()

      val credentials2 = Credentials("ID 1", "Token 2")
      presenter.sendEvent(UpsertCredentials(credentials2))
      expectItem() shouldBe UiModel(listOf(credentials2), Idle)
      expectItem() shouldBe UiModel(listOf(credentials2), Updated(credentials2))

      dispatcher.advanceTimeBy(50)

      val credentials3 = Credentials("ID 2", "Token")
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

      val credentials1 = Credentials("ID", "Token 1")
      presenter.sendEvent(UpsertCredentials(credentials1))
      expectItem() shouldBe UiModel(listOf(credentials1), Idle)
      expectItem() shouldBe UiModel(listOf(credentials1), Added(credentials1))

      dispatcher.advanceTimeBy(50)

      val credentials2 = Credentials("ID", "Token 2")
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
    val credentials1 = Credentials("ID 1", "Token 1")
    store.upsert(credentials1)
    val credentials2 = Credentials("ID 2", "Token 1")
    store.upsert(credentials2)

    presenter.uiModels.test {
      expectItem()

      val credentials3 = Credentials("ID 1", "Token 2")
      presenter.sendEvent(UpsertCredentials(credentials3))
      expectItem() shouldBe UiModel(listOf(credentials3, credentials2), Idle)
      expectItem() shouldBe UiModel(listOf(credentials3, credentials2), Updated(credentials3))

      dispatcher.advanceTimeBy(50)

      val credentials4 = Credentials("ID 2", "Token 2")
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

      val credentials = Credentials("ID", "Token")
      presenter.sendEvent(UpsertCredentials(credentials, showPrompt = false))
      expectItem() shouldBe UiModel(listOf(credentials), Idle)

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
  private fun Credentials(id: String, token: String): Credentials {
    return Credentials.Impl(CredentialsId(id), Token(token))
  }
}
