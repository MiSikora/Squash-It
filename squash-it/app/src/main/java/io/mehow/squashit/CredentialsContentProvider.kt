package io.mehow.squashit

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher.NO_MATCH
import android.database.Cursor
import android.net.Uri
import dagger.android.AndroidInjection
import javax.inject.Inject
import android.content.UriMatcher as AndroidUriMatcher

class CredentialsContentProvider : ContentProvider() {
  @Inject lateinit var database: Database

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
    return database.credentialsQueries.get(CredentialsId(id)).asCursor("id", "secret") {
      listOf(ColumnPrimitive(it.id.value), ColumnPrimitive(it.secret.value))
    }.apply { setNotificationUri(context.contentResolver, uri) }
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
    const val Authority = "io.mehow.squashit.contentprovider"
    const val SingleCredentialsCode = 1
    val UriMatcher = AndroidUriMatcher(NO_MATCH).apply {
      addURI(Authority, "credentials/*", SingleCredentialsCode)
    }
  }
}