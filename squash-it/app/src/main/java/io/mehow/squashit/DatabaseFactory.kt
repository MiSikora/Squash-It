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

  private val tokenAdapter = object : ColumnAdapter<Token, String> {
    override fun decode(databaseValue: String) = Token(databaseValue)
    override fun encode(value: Token): String = value.value
  }

  private val credentialsAdapter = Credentials.Adapter(
      idAdapter = credentialsIdAdapter,
      tokenAdapter = tokenAdapter
  )
}
