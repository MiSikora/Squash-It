package io.mehow.squashit.timber

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import timber.log.Timber

internal class SquashItTimberInitializer : ContentProvider() {
  override fun onCreate(): Boolean {
    Timber.plant(SquashItTimberTree)
    return true
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    return null
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?
  ): Cursor? {
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
}
