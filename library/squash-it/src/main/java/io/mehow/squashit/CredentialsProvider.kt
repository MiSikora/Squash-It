package io.mehow.squashit

import android.content.Context

interface CredentialsProvider {
  fun provide(context: Context): Credentials?
}

@Suppress("FunctionName") // Fake constructor.
fun CredentialsProvider(userId: String): CredentialsProvider = AppCredentialsProvider(userId)
