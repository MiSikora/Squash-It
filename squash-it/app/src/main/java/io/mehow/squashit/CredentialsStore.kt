package io.mehow.squashit

import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class CredentialsStore @Inject constructor(
  private val database: Database,
  @Io private val context: CoroutineContext
) : Transacter by database {
  private val queries = database.credentialsQueries

  suspend fun upsert(credentials: Credentials) = withContext(context) {
    queries.upsert(credentials)
  }

  suspend fun delete(id: CredentialsId) = withContext(context) {
    queries.delete(id)
  }

  suspend fun get(id: CredentialsId) = withContext(context) {
    queries.get(id).executeAsOneOrNull()
  }

  val credentials: Flow<List<Credentials>>
    get() = queries
        .getAll()
        .asFlow()
        .mapToList(context)
        .distinctUntilChanged()

  private fun CredentialsQueries.upsert(credentials: Credentials) = transaction {
    update(credentials.id, credentials.token)
    if (changes().executeAsOne() == 0L) insertOrFail(credentials)
  }
}
