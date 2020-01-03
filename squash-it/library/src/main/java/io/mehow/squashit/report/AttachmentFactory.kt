package io.mehow.squashit.report

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.os.CancellationSignal
import android.provider.DocumentsContract
import android.provider.MediaStore.MediaColumns.MIME_TYPE
import android.provider.OpenableColumns.DISPLAY_NAME
import android.provider.OpenableColumns.SIZE
import androidx.collection.LruCache
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.jakewharton.byteunits.DecimalByteUnit
import com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES
import io.mehow.squashit.report.AttachmentType.Companion.fromMimeType
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.source
import java.io.IOException
import kotlin.coroutines.resume

internal object AttachmentFactory {
  const val RequestCode = 200
  private val bitmapCache = object : LruCache<AttachmentKey, Bitmap>(MEGABYTES.toBytes(2).toInt()) {
    override fun sizeOf(key: AttachmentKey, value: Bitmap): Int {
      return value.byteCount
    }
  }

  suspend fun create(resolver: ContentResolver, id: AttachmentId): Attachment? {
    return suspendCancellableCoroutine { continuation ->
      val signal = CancellationSignal()
      continuation.invokeOnCancellation { signal.cancel() }
      val builder = createBuilder(resolver, id, signal)
      if (builder == null) continuation.resume(null)
      else continuation.resume(builder.toAttachment(resolver))
    }
  }

  suspend fun create(resolver: ContentResolver, key: AttachmentKey): AttachmentItem? {
    return suspendCancellableCoroutine { continuation ->
      val signal = CancellationSignal()
      continuation.invokeOnCancellation { signal.cancel() }
      val builder = createBuilder(resolver, key.id, signal)
      if (builder == null) continuation.resume(null)
      else {
        val thumbnail = getThumbnail(key, resolver, signal)
        continuation.resume(builder.toAttachmentItem(thumbnail))
      }
    }
  }

  private fun createBuilder(
    resolver: ContentResolver,
    id: AttachmentId,
    signal: CancellationSignal
  ): AttachmentBuilder? {
    return resolver.query(id.value.toUri(), null, null, null, null, signal)?.use { cursor ->
      if (!cursor.moveToFirst()) return@use null

      val nameIndex = cursor.getColumnIndex(DISPLAY_NAME)
      val name = cursor.getString(nameIndex)

      val sizeIndex = cursor.getColumnIndex(SIZE)
      val size = cursor.getLongOrNull(sizeIndex)?.let(DecimalByteUnit::format) ?: return@use null

      val mimeIndex = cursor.getColumnIndex(MIME_TYPE)
      val type = cursor.getStringOrNull(mimeIndex)?.let { fromMimeType(it) } ?: return@use null

      return@use AttachmentBuilder(id, type, name, size)
    }
  }

  private fun getThumbnail(
    key: AttachmentKey,
    resolver: ContentResolver,
    signal: CancellationSignal
  ): Bitmap? {
    return bitmapCache.get(key) ?: try {
      val uri = key.id.value.toUri()
      val bitmap = DocumentsContract.getDocumentThumbnail(resolver, uri, key.point, signal)
      if (bitmap != null) {
        synchronized(bitmapCache) {
          if (bitmapCache.get(key) == null) bitmapCache.put(key, bitmap)
        }
      }
      bitmap
    } catch (_: IOException) {
      null
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

  private class AttachmentBuilder(
    val id: AttachmentId,
    val type: AttachmentType,
    val name: String,
    val size: String
  ) {
    fun toAttachment(resolver: ContentResolver): Attachment {
      return Attachment(id, name) {
        resolver.openInputStream(id.value.toUri())?.source()
      }
    }

    fun toAttachmentItem(thumbnail: Bitmap?): AttachmentItem {
      return AttachmentItem(id, type, name, size, thumbnail)
    }
  }
}
