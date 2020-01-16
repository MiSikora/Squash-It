package io.mehow.squashit

import android.content.Context
import androidx.annotation.IntRange
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.User
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import io.mehow.squashit.report.RuntimeInfo as Info

object SquashItConfigurator {
  internal var ProjectKey: String = ""
  internal var JiraUrl: HttpUrl? = null
  internal var SubTaskIssueId: String = "5"
  internal var Credentials: Credentials? = null
  internal var AllowReporterOverride = true
  internal var UserFilter: (User) -> Boolean = { true }
  internal var IssueTypeFilter: (IssueType) -> Boolean = { true }
  internal var FingerTriggerCount: Int = 2
  internal var LogsCapacity: Int = 2_000
  internal var EpicReadFieldName: String = "customfield_10009"
  internal var EpicWriteFieldName: String = "customfield_10008"
  internal var RuntimeInfo: Info = Info.Null
  private var credentialsProvider: CredentialsProvider = object : CredentialsProvider {
    override fun provide(context: Context): Credentials? = null
  }

  @JvmStatic fun projectKey(key: String): SquashItConfigurator {
    ProjectKey = key.trim()
    return this
  }

  @JvmStatic fun jiraUrl(url: String): SquashItConfigurator {
    JiraUrl = url.trim().toHttpUrlOrNull()
    return this
  }

  @JvmStatic fun subTaskIssueId(id: String): SquashItConfigurator {
    SubTaskIssueId = id
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
    AllowReporterOverride = allow
    return this
  }

  @JvmStatic fun userFilter(filter: (User) -> Boolean): SquashItConfigurator {
    UserFilter = filter
    return this
  }

  @JvmStatic fun issueTypeFilter(filter: (IssueType) -> Boolean): SquashItConfigurator {
    IssueTypeFilter = filter
    return this
  }

  @JvmStatic fun fingerTriggerCount(@IntRange(from = 1) count: Int): SquashItConfigurator {
    require(count >= 1) { "Finger trigger count must be at least 1." }
    FingerTriggerCount = count
    return this
  }

  @JvmStatic fun logsCapacity(@IntRange(from = 1) capacity: Int): SquashItConfigurator {
    require(capacity >= 1) { "Logs capacity must be at least 1." }
    LogsCapacity = capacity
    return this
  }

  @JvmStatic fun epicReadFieldName(name: String): SquashItConfigurator {
    EpicReadFieldName = name.trim()
    return this
  }

  @JvmStatic fun epicWriteFieldName(name: String): SquashItConfigurator {
    EpicWriteFieldName = name.trim()
    return this
  }

  @JvmStatic fun configure(context: Context) {
    RuntimeInfo = Info.create(context.applicationContext)
    Credentials = credentialsProvider.provide(context)
    SquashItConfig.configure()
  }
}
