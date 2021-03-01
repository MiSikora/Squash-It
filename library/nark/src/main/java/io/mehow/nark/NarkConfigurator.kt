package io.mehow.nark

import androidx.annotation.IntRange
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

public object NarkConfigurator {
  internal var projectKey: String = ""

  public fun projectKey(key: String): NarkConfigurator = apply {
    this.projectKey = key
  }

  internal var jiraUrl: HttpUrl = "https://github.com/MiSikora/nark".toHttpUrl()

  public fun jiraUrl(url: HttpUrl): NarkConfigurator = apply {
    this.jiraUrl = url
  }

  internal var subTaskIssueId: String = "5"

  public fun subTaskIssueId(id: String): NarkConfigurator = apply {
    this.subTaskIssueId = id
  }

  internal var credentialsProvider: CredentialsProvider = CredentialsProvider { Credentials.Empty }

  public fun credentialsProvider(provider: CredentialsProvider): NarkConfigurator = apply {
    this.credentialsProvider = provider
  }

  internal var isReporterOverrideEnabled: Boolean = false

  public fun enableReporterOverride(enable: Boolean): NarkConfigurator = apply {
    this.isReporterOverrideEnabled = enable
  }

  internal var userFilter: (User) -> Boolean = { true }

  public fun userFilter(filter: (User) -> Boolean): NarkConfigurator = apply {
    this.userFilter = filter
  }

  internal var issueTypeFilter: (IssueType) -> Boolean = { true }

  public fun issueTypeFilter(filter: (IssueType) -> Boolean): NarkConfigurator = apply {
    this.issueTypeFilter = filter
  }

  internal var epicReadFieldName: String = "customfield_10009"

  public fun epicReadFieldName(name: String): NarkConfigurator = apply {
    this.epicReadFieldName = name
  }

  internal var epicWriteFieldName: String = "customfield_10008"

  public fun epicWriteFieldName(name: String): NarkConfigurator = apply {
    this.epicWriteFieldName = name
  }

  @OptIn(ExperimentalTime::class)
  private var screenshotDelay: Duration = 400.milliseconds

  public fun screenshotDelay(delayMillis: Long): NarkConfigurator = apply {
    @OptIn(ExperimentalTime::class) screenshotDelay(delayMillis.milliseconds)
  }

  @ExperimentalTime
  public fun screenshotDelay(delay: Duration): NarkConfigurator = apply {
    this.screenshotDelay = delay
  }

  private var logsCapacity: Int = 2_000

  public fun logsCapacity(@IntRange(from = 1) capacity: Int): NarkConfigurator = apply {
    this.logsCapacity = capacity
  }

  private val isConfigured = AtomicBoolean()

  public fun configure() {
    check(!isConfigured.getAndSet(true)) { "Nark can be configured only once" }
    Nark.Instance = Nark(this)
  }
}

