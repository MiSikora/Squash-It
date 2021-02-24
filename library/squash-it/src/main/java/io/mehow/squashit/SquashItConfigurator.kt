package io.mehow.squashit

import android.content.Context
import androidx.annotation.IntRange
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.User
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import io.mehow.squashit.report.RuntimeInfo as Info

object SquashItConfigurator {
  internal var projectKey: String = ""
  internal var jiraUrl: HttpUrl? = null
  internal var subTaskIssueId: String = "5"
  internal var credentials: Credentials? = null
  internal var allowReporterOverride = true
  internal var uUserFilter: (User) -> Boolean = { true }
  internal var issueTypeFilter: (IssueType) -> Boolean = { true }
  internal var logsCapacity: Int = 2_000
  internal var epicReadFieldName: String = "customfield_10009"
  internal var epicWriteFieldName: String = "customfield_10008"
  internal var runtimeInfo: Info = Info.Null
  private var credentialsProvider: CredentialsProvider = object : CredentialsProvider {
    override fun provide(context: Context): Credentials? = null
  }

  @JvmStatic fun projectKey(key: String): SquashItConfigurator {
    projectKey = key.trim()
    return this
  }

  @JvmStatic fun jiraUrl(url: String): SquashItConfigurator {
    jiraUrl = url.trim().toHttpUrlOrNull()
    return this
  }

  @JvmStatic fun subTaskIssueId(id: String): SquashItConfigurator {
    subTaskIssueId = id
    return this
  }

  @JvmStatic fun credentialsProvider(provider: (Context) -> Credentials?): SquashItConfigurator {
    credentialsProvider = object : CredentialsProvider {
      override fun provide(context: Context) = provider(context)
    }
    return this
  }

  @JvmStatic fun credentialsProvider(provider: CredentialsProvider): SquashItConfigurator {
    credentialsProvider = provider
    return this
  }

  @JvmStatic fun allowReporterOverride(allow: Boolean): SquashItConfigurator {
    allowReporterOverride = allow
    return this
  }

  @JvmStatic fun userFilter(filter: (User) -> Boolean): SquashItConfigurator {
    uUserFilter = filter
    return this
  }

  @JvmStatic fun issueTypeFilter(filter: (IssueType) -> Boolean): SquashItConfigurator {
    issueTypeFilter = filter
    return this
  }

  @JvmStatic fun logsCapacity(@IntRange(from = 1) capacity: Int): SquashItConfigurator {
    require(capacity >= 1) { "Logs capacity must be at least 1." }
    logsCapacity = capacity
    return this
  }

  @JvmStatic fun epicReadFieldName(name: String): SquashItConfigurator {
    epicReadFieldName = name.trim()
    return this
  }

  @JvmStatic fun epicWriteFieldName(name: String): SquashItConfigurator {
    epicWriteFieldName = name.trim()
    return this
  }

  @JvmStatic fun configure(context: Context) {
    runtimeInfo = Info.create(context.applicationContext)
    credentials = credentialsProvider.provide(context)
    SquashItConfig.configure()
  }
}
