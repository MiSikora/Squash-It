package io.mehow.squashit.api

import io.mehow.squashit.api.extensions.asResponse
import java.util.ArrayDeque

internal class EpicsFactory {
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

  fun create(): Response<EpicsResponse> {
    val records = queue.poll() ?: listOf(Record("Epic ID", "Epic Name"))
    val epics = records.map { (id, name) -> EpicIssueResponse(id, EpicFieldsResponse(name)) }
    return EpicsResponse(epics).asResponse(returnErrors)
  }

  data class Record(val id: String, val name: String)
}
