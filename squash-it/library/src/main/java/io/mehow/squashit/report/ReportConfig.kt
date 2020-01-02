package io.mehow.squashit.report

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import io.mehow.squashit.R
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File

internal sealed class ReportConfig : Parcelable {
  abstract fun startActivity(activity: Activity, screenshotFile: File?)

  @Parcelize
  data class Invalid(
    val hasProjectKey: Boolean,
    val hasJiraUrl: Boolean,
    val hasUserEmail: Boolean,
    val hasUserToken: Boolean
  ) : ReportConfig() {
    override fun startActivity(activity: Activity, screenshotFile: File?) {
      MisconfigurationActivity.start(activity, MisconfigurationActivity.Args(this))
    }
  }

  @Parcelize
  @TypeParceler<HttpUrl, HttpUrlParceler>
  data class Valid(
    val projectKey: String,
    val jiraUrl: HttpUrl,
    val userEmail: String,
    val userToken: String,
    val whitelistUsers: Boolean,
    val filteredUsers: List<String>,
    val whitelistIssueTypes: Boolean,
    val filteredIssueTypes: List<String>,
    val runtimeInfo: RuntimeInfo,
    val epicReadFieldName: String,
    val epicWriteFieldName: String
  ) : ReportConfig() {
    override fun startActivity(activity: Activity, screenshotFile: File?) {
      ReportActivity.start(activity, ReportActivity.Args(this, screenshotFile))
    }

    fun filterUser(user: User): Boolean {
      return if (whitelistUsers) user.accountId in filteredUsers
      else user.accountId !in filteredUsers
    }

    fun filterIssuesTypes(issue: IssueType): Boolean {
      return if (whitelistIssueTypes) issue.id in filteredIssueTypes
      else issue.id !in filteredIssueTypes
    }
  }

  companion object {
    @Suppress("LongMethod")
    fun create(activity: Activity): ReportConfig {
      val projectKey = activity.getString(R.string.squash_it_jira_project_key).trim()
      val jiraUrl = activity.getString(R.string.squash_it_jira_server_url).trim().toHttpUrlOrNull()
      val userEmail = activity.getString(R.string.squash_it_jira_user_email).trim()
      val userToken = activity.getString(R.string.squash_it_jira_user_token).trim()
      val filteredUsers = activity.resources
          .getStringArray(R.array.squash_it_jira_user_filter)
          .map(String::trim)
      val filteredIssueTypes = activity.resources
          .getStringArray(R.array.squash_it_jira_issue_types_filter)
          .map(String::trim)
      val epicReadFieldName = activity.getString(R.string.squash_it_jira_epic_read_field_name)
      val epicWriteFieldName = activity.getString(R.string.squash_it_jira_epic_write_field_name)

      val hasProjectKey = projectKey.isNotEmpty()
      val hasJiraUrl = jiraUrl != null
      val hasUserEmail = userEmail.isNotEmpty()
      val hasUserToken = userToken.isNotEmpty()

      val isInvalid = listOf(hasProjectKey, hasJiraUrl, hasUserEmail, hasUserToken).any { !it }

      val whitelistUsers = activity.resources
          .getBoolean(R.bool.squash_it_whitelist_jira_user)
      val whitelistIssueTypes = activity.resources
          .getBoolean(R.bool.squash_it_whitelist_issue_types)

      return if (isInvalid) Invalid(hasProjectKey, hasJiraUrl, hasUserEmail, hasUserToken)
      else Valid(
          projectKey = projectKey,
          jiraUrl = jiraUrl!!,
          userEmail = userEmail,
          userToken = userToken,
          whitelistUsers = whitelistUsers,
          filteredUsers = filteredUsers,
          whitelistIssueTypes = whitelistIssueTypes,
          filteredIssueTypes = filteredIssueTypes,
          runtimeInfo = RuntimeInfo.create(activity),
          epicReadFieldName = epicReadFieldName,
          epicWriteFieldName = epicWriteFieldName
      )
    }
  }

  private object HttpUrlParceler : Parceler<HttpUrl> {
    override fun create(parcel: Parcel) = parcel.readString()!!.toHttpUrl()
    override fun HttpUrl.write(parcel: Parcel, flags: Int) = parcel.writeString("$this")
  }
}
