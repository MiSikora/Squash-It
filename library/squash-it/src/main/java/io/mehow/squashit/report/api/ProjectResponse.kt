package io.mehow.squashit.report.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ProjectResponse(
  val issueTypes: List<IssueTypeResponse>,
  val roles: Map<String, String>
) {
  val roleIds = roles.values.map { it.substringAfterLast('/') }
}
