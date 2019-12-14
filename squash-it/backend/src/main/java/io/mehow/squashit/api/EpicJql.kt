package io.mehow.squashit.api

data class EpicJql(
  val projectKey: String
) {
  override fun toString(): String {
    return "issuetype=\"Epic\" and project=$projectKey"
  }
}
