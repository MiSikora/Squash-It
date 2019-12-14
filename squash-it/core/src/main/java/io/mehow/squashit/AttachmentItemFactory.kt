package io.mehow.squashit

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.MediaColumns.MIME_TYPE
import android.provider.OpenableColumns.DISPLAY_NAME
import android.provider.OpenableColumns.SIZE
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.jakewharton.byteunits.DecimalByteUnit
import io.mehow.squashit.AttachmentType.Companion.fromMimeType
import okio.source

internal object AttachmentItemFactory {
  const val RequestCode = 200

  fun create(contentResolver: ContentResolver, uri: Uri): AttachmentItem? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (!cursor.moveToFirst()) return@use null

      val nameIndex = cursor.getColumnIndex(DISPLAY_NAME)
      val name = cursor.getString(nameIndex)

      val sizeIndex = cursor.getColumnIndex(SIZE)
      val size = cursor.getLongOrNull(sizeIndex)?.let(DecimalByteUnit::format) ?: return@use null

      val mimeIndex = cursor.getColumnIndex(MIME_TYPE)
      val type = cursor.getStringOrNull(mimeIndex)?.let { fromMimeType(it) } ?: return@use null

      contentResolver.openInputStream(uri)
      return@use AttachmentItem(type, name, size) {
        contentResolver.openInputStream(uri)?.source()
      }
    }
  }

  fun requestAttachment(activity: Activity) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "*/*"
      putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
      putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
    }
    activity.startActivityForResult(intent, RequestCode)
  }
}
