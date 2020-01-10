package io.mehow.squashit

import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
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

    store.upsert("ID", "Token")

    store.get("ID") shouldBe Credentials("ID", "Token")
  }

  @Test fun `credentials can be updated`() = test {
    store.upsert("ID", "Token 1")
    store.upsert("ID", "Token 2")

    store.get("ID") shouldBe Credentials("ID", "Token 2")
  }

  @Test fun `credentials can have same tokens`() = test {
    store.upsert("ID 1", "Token")
    store.upsert("ID 2", "Token")

    store.get("ID 1") shouldBe Credentials("ID 1", "Token")
    store.get("ID 2") shouldBe Credentials("ID 2", "Token")
  }

  @Test fun `credentials can be deleted`() = test {
    store.upsert("ID", "Token")

    store.delete("ID")

    store.get("ID").shouldBeNull()
  }

  @Test fun `deleting non-existing credentials does not fail`() = test {
    store.upsert("ID 1", "Token")

    store.delete("ID 2")

    store.get("ID 1") shouldBe Credentials("ID 1", "Token")
  }

  @Test fun `credentials can be observed`() = test {
    store.credentials.test {
      expectItem() shouldBe emptyList()

      store.upsert("ID 1", "Token 1")
      expectItem() shouldBe listOf("ID 1" to "Token 1").map(::toCredentials)

      store.upsert("ID 2", "Token 2")
      expectItem() shouldBe listOf("ID 1" to "Token 1", "ID 2" to "Token 2").map(::toCredentials)

      store.upsert("ID 1", "Token 3")
      expectItem() shouldBe listOf("ID 1" to "Token 3", "ID 2" to "Token 2").map(::toCredentials)

      store.delete("ID 2")
      expectItem() shouldBe listOf("ID 1" to "Token 3").map(::toCredentials)

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
  private fun Credentials(id: String, token: String): Credentials {
    return Credentials.Impl(CredentialsId(id), Token(token))
  }

  private fun toCredentials(tuple: Pair<String, String>): Credentials {
    return Credentials(tuple.first, tuple.second)
  }

  private suspend fun CredentialsStore.upsert(id: String, token: String) {
    upsert(Credentials(id, token))
  }

  private suspend fun CredentialsStore.get(id: String): Credentials? {
    return get(CredentialsId(id))
  }

  private suspend fun CredentialsStore.delete(id: String) {
    delete(CredentialsId(id))
  }
}
