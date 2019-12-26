package io.mehow.squashit.api

import io.mehow.squashit.api.extensions.asResponse
import java.util.ArrayDeque

internal class NewIssueFactory {
  private val queue = ArrayDeque<String>()
  private var returnErrors = false

  fun enableErrors() {
    returnErrors = true
  }

  fun disableErrors() {
    returnErrors = false
  }

  fun enqueue(id: String) {
    queue.add(id)
  }

  fun create(): Response<CreateNewIssueResponse> {
    val id = queue.poll() ?: "Issue ID"
    return CreateNewIssueResponse(id).asResponse(returnErrors)
  }
}
