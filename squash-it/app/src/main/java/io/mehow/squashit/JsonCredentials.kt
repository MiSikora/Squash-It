package io.mehow.squashit

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonCredentials(val id: String, val secret: String) {
  fun asCredentials() = Credentials.Impl(CredentialsId(id), Secret(secret))

  companion object {
    fun fromCredentials(credentials: Credentials): JsonCredentials {
      return JsonCredentials(credentials.id.value, credentials.secret.value)
    }
  }
}
