package io.mehow.squashit.report

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ProjectInfo(
  val epics: Set<Epic>,
  val users: Set<User>,
  val issueTypes: Set<IssueType>
)
