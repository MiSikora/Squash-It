package io.mehow.nark

import android.content.Context
import androidx.core.net.toUri

internal class LegacyCredentialsProvider(
  private val context: Context,
  userId: String,
) : CredentialsProvider {
  private val uri = "content://io.mehow.squashit.contentprovider/credentials/$userId".toUri()

  override fun provide() = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
    if (!cursor.moveToFirst()) return@use null

    val idIndex = cursor.getColumnIndex("id")
    val id = cursor.getString(idIndex)

    val secretIndex = cursor.getColumnIndex("secret")
    val secret = cursor.getString(secretIndex)

    Credentials(id, secret)
  }
}
