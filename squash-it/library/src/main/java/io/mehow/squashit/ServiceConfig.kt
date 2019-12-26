package io.mehow.squashit

import okhttp3.HttpUrl

internal data class ServiceConfig(
  val projectKey: String,
  val jiraUrl: HttpUrl,
  val userEmail: String,
  val userToken: String,
  val filteredUsers: List<String>,
  val filteredIssueTypes: List<String>,
  val runtimeInfo: RuntimeInfo,
  val epicReadFieldName: String,
  val epicWriteFieldName: String
)
