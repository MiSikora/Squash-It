package io.mehow.squashit

import android.content.Context
import androidx.core.net.toUri

internal class AppCredentialsProvider(private val userId: String) : CredentialsProvider {
  override fun provide(context: Context): Credentials? {
    val uri = "content://io.mehow.squashit.contentprovider/credentials/$userId".toUri()
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (!cursor.moveToFirst()) return@use null

      val idIndex = cursor.getColumnIndex("id")
      val id = cursor.getString(idIndex)

      val secretIndex = cursor.getColumnIndex("secret")
      val secret = cursor.getString(secretIndex)

      return Credentials(id, secret)
    }
  }
}
