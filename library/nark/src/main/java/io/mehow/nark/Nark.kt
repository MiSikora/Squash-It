package io.mehow.nark

import okhttp3.HttpUrl

internal data class Nark(
  val projectKey: String,
  val jiraUrl: HttpUrl,
  val subTaskIssueId: String,
  val credentials: Credentials,
  val isReporterOverrideEnabled: Boolean,
  val userFilter: (User) -> Boolean,
  val issueTypeFilter: (IssueType) -> Boolean,
  val epicReadFieldName: String,
  val epicWriteFieldName: String,
) {
  constructor(configurator: NarkConfigurator) : this(
      projectKey = configurator.projectKey,
      jiraUrl = configurator.jiraUrl,
      subTaskIssueId = configurator.subTaskIssueId,
      credentials = configurator.credentialsProvider.provide() ?: Credentials.Empty,
      isReporterOverrideEnabled = configurator.isReporterOverrideEnabled,
      userFilter = configurator.userFilter,
      issueTypeFilter = configurator.issueTypeFilter,
      epicWriteFieldName = configurator.epicWriteFieldName,
      epicReadFieldName = configurator.epicReadFieldName,
  )

  companion object {
    var Instance = Nark(NarkConfigurator)
  }
}
