package io.mehow.squashit

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver

object DatabaseFactory {
  fun create(driver: SqlDriver): Database {
    return Database(driver, credentialsAdapter)
  }

  private val credentialsIdAdapter = object : ColumnAdapter<CredentialsId, String> {
    override fun decode(databaseValue: String) = CredentialsId(databaseValue)
    override fun encode(value: CredentialsId): String = value.value
  }

  private val secretAdapter = object : ColumnAdapter<Secret, String> {
    override fun decode(databaseValue: String) = Secret(databaseValue)
    override fun encode(value: Secret): String = value.value
  }

  private val credentialsAdapter = Credentials.Adapter(
      idAdapter = credentialsIdAdapter,
      secretAdapter = secretAdapter
  )
}
