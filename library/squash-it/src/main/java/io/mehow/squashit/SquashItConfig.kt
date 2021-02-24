package io.mehow.squashit

import android.annotation.SuppressLint
import android.app.Activity
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.MisconfigurationActivity
import io.mehow.squashit.report.ReportActivity
import io.mehow.squashit.report.RuntimeInfo
import io.mehow.squashit.report.User
import okhttp3.HttpUrl
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

internal data class SquashItConfig(
  val projectKey: String,
  val jiraUrl: HttpUrl?,
  val subTaskIssueId: String,
  val credentials: Credentials,
  val allowReporterOverride: Boolean,
  val userFilter: (User) -> Boolean,
  val issueTypeFilter: (IssueType) -> Boolean,
  val epicReadFieldName: String,
  val epicWriteFieldName: String,
  val runtimeInfo: RuntimeInfo,
) {
  constructor(configurator: SquashItConfigurator) : this(
      projectKey = configurator.projectKey,
      jiraUrl = configurator.jiraUrl,
      subTaskIssueId = configurator.subTaskIssueId,
      credentials = configurator.credentials ?: Credentials("", ""),
      allowReporterOverride = configurator.allowReporterOverride,
      userFilter = configurator.uUserFilter,
      issueTypeFilter = configurator.issueTypeFilter,
      epicWriteFieldName = configurator.epicWriteFieldName,
      epicReadFieldName = configurator.epicReadFieldName,
      runtimeInfo = configurator.runtimeInfo
  )

  val hasProjectKey = projectKey.isNotEmpty()
  val hasJiraUrl = jiraUrl != null
  val hasUserId = credentials.id.isNotBlank()
  val hasUserSecret = credentials.secret.isNotEmpty()

  private val isInvalid: Boolean
    get() {
      return listOf(hasProjectKey, hasJiraUrl, hasUserId, hasUserSecret).any { !it }
    }

  fun start(activity: Activity, screenshotFile: File?) {
    if (isInvalid) MisconfigurationActivity.start(activity)
    else ReportActivity.start(activity, ReportActivity.Args(screenshotFile))
  }

  companion object {
    private val isConfigured = AtomicBoolean()

    internal const val Name = "SquashIt"

    fun configure() {
      @SuppressLint("SyntheticAccessor") // Lint is wrong.
      if (isConfigured.getAndSet(true)) {
        error("Plugin can be configured only once.")
      }
      SquashItLogger.setLogsCapacity(SquashItConfigurator.logsCapacity)
      Instance = SquashItConfig(SquashItConfigurator)
    }

    var Instance: SquashItConfig = SquashItConfig(SquashItConfigurator)
      private set
  }
}
