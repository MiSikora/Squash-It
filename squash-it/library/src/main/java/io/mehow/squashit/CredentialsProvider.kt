package io.mehow.squashit

import android.content.Context

interface CredentialsProvider {
  fun provide(context: Context): Credentials?

  companion object {
    operator fun invoke(userId: String): CredentialsProvider = AppCredentialsProvider(userId)
  }
}
