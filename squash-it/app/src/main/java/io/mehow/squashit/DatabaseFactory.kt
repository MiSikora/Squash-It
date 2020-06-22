package io.mehow.squashit

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver

object DatabaseFactory {
  fun create(driver: SqlDriver): Database {
    return Database(driver, CredentialsAdapter)
  }

  private val CredentialsIdAdapter = object : ColumnAdapter<CredentialsId, String> {
    override fun decode(databaseValue: String) = CredentialsId(databaseValue)
    override fun encode(value: CredentialsId): String = value.value
  }

  private val SecretAdapter = object : ColumnAdapter<Secret, String> {
    override fun decode(databaseValue: String) = Secret(databaseValue)
    override fun encode(value: Secret): String = value.value
  }

  private val CredentialsAdapter = Credentials.Adapter(
    idAdapter = CredentialsIdAdapter,
    secretAdapter = SecretAdapter
  )
}
