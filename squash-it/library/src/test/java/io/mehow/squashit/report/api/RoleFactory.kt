package io.mehow.squashit.report.api

import io.mehow.squashit.report.api.extensions.asResponse
import java.util.ArrayDeque

internal class RoleFactory {
  private val queue = ArrayDeque<List<Record>>()
  private var returnErrors = false

  fun enableErrors() {
    returnErrors = true
  }

  fun disableErrors() {
    returnErrors = false
  }

  fun enqueue(vararg records: Record) {
    queue.add(records.toList())
  }

  fun create(): Response<RoleResponse> {
    val records = queue.poll() ?: listOf(Record("User Name", "User ID"))
    val actors = records
        .map { ActorResponse(it.name, it.id?.let { id -> ActorUserResponse(id) }) }
    return RoleResponse(actors).asResponse(returnErrors)
  }

  data class Record(val name: String, val id: String?)
}
