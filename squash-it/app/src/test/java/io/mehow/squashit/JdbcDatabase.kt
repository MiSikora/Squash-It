package io.mehow.squashit

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver.Companion.IN_MEMORY
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class JdbcDatabase private constructor(
  private val driver: JdbcSqliteDriver,
  database: Database
) : Database by database, TestRule {
  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        driver.use {
          Database.Schema.create(it)
          base.evaluate()
        }
      }
    }
  }

  companion object {
    operator fun invoke(): JdbcDatabase {
      val driver = JdbcSqliteDriver(IN_MEMORY)
      val database = DatabaseFactory.create(driver)
      return JdbcDatabase(driver, database)
    }
  }
}
