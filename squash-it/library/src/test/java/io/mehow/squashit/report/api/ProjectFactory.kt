package io.mehow.squashit.report.api

import io.mehow.squashit.report.api.extensions.asResponse
import java.util.ArrayDeque

internal class ProjectFactory {
  private val queue = ArrayDeque<ProjectResponse>()
  private var returnErrors = false

  fun enableErrors() {
    returnErrors = true
  }

  fun disableErrors() {
    returnErrors = false
  }

  fun enqueue(record: ProjectResponse) {
    queue.add(record)
  }

  fun create(): Response<ProjectResponse> {
    val project = queue.poll() ?: ProjectResponse(
        listOf(IssueTypeResponse("Issue ID", "Issue Name", false)),
        mapOf("Role Name" to "Role ID")
    )
    return project.asResponse(returnErrors)
  }
}
