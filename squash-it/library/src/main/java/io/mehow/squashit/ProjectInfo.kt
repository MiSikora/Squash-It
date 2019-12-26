package io.mehow.squashit

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ProjectInfo(
  val epics: Set<Epic>,
  val users: Set<User>,
  val issueTypes: Set<IssueType>
)
