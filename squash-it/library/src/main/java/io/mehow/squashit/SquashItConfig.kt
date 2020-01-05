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
  val userEmail: String,
  val userToken: String,
  val userFilter: (User) -> Boolean,
  val issueTypeFilter: (IssueType) -> Boolean,
  val fingerTriggerCount: Int,
  val epicReadFieldName: String,
  val epicWriteFieldName: String,
  val runtimeInfo: RuntimeInfo
) {
  constructor(initializer: SquashItConfigurator) : this(
      projectKey = initializer.ProjectKey,
      jiraUrl = initializer.JiraUrl,
      userEmail = initializer.UserEmail,
      userToken = initializer.UserToken,
      userFilter = initializer.UserFilter,
      issueTypeFilter = initializer.IssueTypeFilter,
      fingerTriggerCount = initializer.FingerTriggerCount,
      epicWriteFieldName = initializer.EpicWriteFieldName,
      epicReadFieldName = initializer.EpicReadFieldName,
      runtimeInfo = initializer.RuntimeInfo
  )

  val hasProjectKey = projectKey.isNotEmpty()
  val hasJiraUrl = jiraUrl != null
  val hasUserEmail = userEmail.isNotEmpty()
  val hasUserToken = userToken.isNotEmpty()

  private val isInvalid: Boolean
    get() {
      return listOf(hasProjectKey, hasJiraUrl, hasUserEmail, hasUserToken).any { !it }
    }

  fun start(activity: Activity, screenshotFile: File?) {
    if (isInvalid) MisconfigurationActivity.start(activity)
    else ReportActivity.start(activity, ReportActivity.Args(screenshotFile))
  }

  companion object {
    private val initialized = AtomicBoolean()

    fun configure() {
      @SuppressLint("SyntheticAccessor") // Lint is wrong.
      if (initialized.getAndSet(true)) {
        error("Plugin can be initialized only once.")
      }
      SquashItLogger.setLogsCapacity(SquashItConfigurator.LogsCapacity)
      Instance = SquashItConfig(SquashItConfigurator)
    }

    var Instance: SquashItConfig = SquashItConfig(SquashItConfigurator)
      private set
  }
}
