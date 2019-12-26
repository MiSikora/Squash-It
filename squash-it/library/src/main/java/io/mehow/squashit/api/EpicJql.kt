package io.mehow.squashit.api

internal data class EpicJql(
  val projectKey: String
) {
  override fun toString(): String {
    return "issuetype=\"Epic\" and project=$projectKey"
  }
}
