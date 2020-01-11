package io.mehow.squashit.external

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher.NO_MATCH
import android.database.Cursor
import android.net.Uri
import dagger.android.AndroidInjection
import io.mehow.squashit.BuildConfig
import javax.inject.Inject
import android.content.UriMatcher as AndroidUriMatcher

class CredentialsContentProvider : ContentProvider() {
  @Inject lateinit var dao: CredentialsDao

  override fun onCreate(): Boolean {
    AndroidInjection.inject(this)
    return true
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?
  ): Cursor? {
    require(UriMatcher.match(uri) == SingleCredentialsCode) { "Unknown URI: $uri" }
    val context = context ?: return null
    val id = requireNotNull(uri.lastPathSegment) { "Missing ID in URI: $uri" }
    return dao.select(id).apply {
      setNotificationUri(context.contentResolver, uri)
    }
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    return null
  }

  override fun update(
    uri: Uri,
    values: ContentValues?,
    selection: String?,
    selectionArgs: Array<out String>?
  ): Int {
    return 0
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
    return 0
  }

  override fun getType(uri: Uri): String? {
    return null
  }

  private companion object {
    const val Authority = "${BuildConfig.APPLICATION_ID}.provider"
    const val SingleCredentialsCode = 1
    val UriMatcher = AndroidUriMatcher(NO_MATCH).apply {
      addURI(Authority, "credentials/*", SingleCredentialsCode)
    }
  }
}
