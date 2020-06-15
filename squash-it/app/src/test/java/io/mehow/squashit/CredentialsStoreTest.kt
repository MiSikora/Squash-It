package io.mehow.squashit

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class CredentialsStoreTest {
  @get:Rule val database = JdbcDatabase()
  private val dispatcher = TestCoroutineDispatcher()
  private val store = CredentialsStore(database, dispatcher)

  @Test fun `credentials can be inserted`() = test {
    store.get("ID").shouldBeNull()

    store.upsert("ID", "Secret")

    store.get("ID") shouldBe Credentials("ID", "Secret")
  }

  @Test fun `credentials can be updated`() = test {
    store.upsert("ID", "Secret 1")
    store.upsert("ID", "Secret 2")

    store.get("ID") shouldBe Credentials("ID", "Secret 2")
  }

  @Test fun `credentials can have same secrets`() = test {
    store.upsert("ID 1", "Secret")
    store.upsert("ID 2", "Secret")

    store.get("ID 1") shouldBe Credentials("ID 1", "Secret")
    store.get("ID 2") shouldBe Credentials("ID 2", "Secret")
  }

  @Test fun `credentials can be deleted`() = test {
    store.upsert("ID", "Secret")

    store.delete("ID")

    store.get("ID").shouldBeNull()
  }

  @Test fun `deleting non-existing credentials does not fail`() = test {
    store.upsert("ID 1", "Secret")

    store.delete("ID 2")

    store.get("ID 1") shouldBe Credentials("ID 1", "Secret")
  }

  @Test fun `credentials can be observed`() = test {
    store.credentials.test {
      expectItem() shouldBe emptyList()

      store.upsert("ID 1", "Secret 1")
      expectItem() shouldBe listOf("ID 1" to "Secret 1").map(::toCredentials)

      store.upsert("ID 2", "Secret 2")
      expectItem() shouldBe listOf("ID 1" to "Secret 1", "ID 2" to "Secret 2").map(::toCredentials)

      store.upsert("ID 1", "Secret 3")
      expectItem() shouldBe listOf("ID 1" to "Secret 3", "ID 2" to "Secret 2").map(::toCredentials)

      store.delete("ID 2")
      expectItem() shouldBe listOf("ID 1" to "Secret 3").map(::toCredentials)

      cancel()
    }
  }

  @Test fun `credentials are ordered alphabetically by ID`() = test {
    store.upsert("e", "")
    store.upsert("a", "")
    store.upsert("c", "")
    store.upsert("d", "")
    store.upsert("b", "")

    store.credentials.first() shouldBe listOf("a", "b", "c", "d", "e").map { Credentials(it, "") }
  }

  private fun test(block: suspend TestCoroutineScope.() -> Unit) {
    dispatcher.runBlockingTest(block)
  }

  @Suppress("TestFunctionName")
  private fun Credentials(id: String, secret: String): Credentials {
    return Credentials.Impl(CredentialsId(id), Secret(secret))
  }

  private fun toCredentials(tuple: Pair<String, String>): Credentials {
    return Credentials(tuple.first, tuple.second)
  }

  private suspend fun CredentialsStore.upsert(id: String, secret: String) {
    upsert(Credentials(id, secret))
  }

  private suspend fun CredentialsStore.get(id: String): Credentials? {
    return get(CredentialsId(id))
  }

  private suspend fun CredentialsStore.delete(id: String) {
    delete(CredentialsId(id))
  }
}
