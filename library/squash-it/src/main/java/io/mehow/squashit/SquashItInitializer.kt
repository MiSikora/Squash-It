package io.mehow.squashit

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import io.mehow.squashit.screenshot.ScreenshotActivity
import io.mehow.squashit.screenshot.ScreenshotActivity.Args
import io.mehow.squashit.screenshot.ScreenshotFactory
import io.mehow.squashit.screenshot.capture.CaptureCallback

internal class SquashItInitializer : ContentProvider() {
  override fun onCreate(): Boolean {
    val application = context?.applicationContext as? Application ?: return false
    SquashItLogger.cleanUp(application)
    ScreenshotFactory.cleanUp(application)
    val screenshotCallback = CaptureCallback { activity, screenshot ->
      if (screenshot == null) SquashItConfig.Instance.start(activity, screenshot)
      else ScreenshotActivity.start(activity, Args(screenshot))
    }
    application.registerActivityLifecycleCallbacks(screenshotCallback)
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
