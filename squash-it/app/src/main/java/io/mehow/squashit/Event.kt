package io.mehow.squashit

sealed class Event {
  data class UpsertCredentials(
    val credentials: Credentials,
    val showPrompt: Boolean = true
  ) : Event()

  data class DeleteCredentials(val id: CredentialsId) : Event()
}
