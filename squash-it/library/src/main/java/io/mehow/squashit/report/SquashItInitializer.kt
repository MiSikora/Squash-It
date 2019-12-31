package io.mehow.squashit.report

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.mattprecious.telescope.TelescopeLayout
import io.mehow.squashit.R
import io.mehow.squashit.SquashItLogger
import io.mehow.squashit.TelescopeCallback
import io.mehow.squashit.screenshot.ScreenshotFactory

internal class SquashItInitializer : ContentProvider() {
  override fun onCreate(): Boolean {
    val application = context?.applicationContext as? Application ?: return false
    TelescopeLayout.cleanUp(application)
    SquashItLogger.cleanUp(application)
    val logsCapacity = application.resources.getInteger(R.integer.squash_it_logs_capacity)
    SquashItLogger.setLogsCapacity(logsCapacity)
    ScreenshotFactory.cleanUp(application)
    application.registerActivityLifecycleCallbacks(TelescopeCallback)
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
