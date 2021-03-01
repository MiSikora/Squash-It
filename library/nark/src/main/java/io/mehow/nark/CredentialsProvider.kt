package io.mehow.nark

import android.content.Context

public fun interface CredentialsProvider {
  public fun provide(): Credentials?

  public companion object {
    public fun external(
      context: Context,
      userId: String,
    ): CredentialsProvider = ExternalCredentialsProvider(context, userId)

    @Deprecated(
        message = "Support for legacy project will be dropped in 1.0.0.",
        replaceWith = ReplaceWith("CredentialsProvider.external(context, userId)"),
    )
    public fun legacy(
      context: Context,
      userId: String,
    ): CredentialsProvider = LegacyCredentialsProvider(context, userId)
  }
}
