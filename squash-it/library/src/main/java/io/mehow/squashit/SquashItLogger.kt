package io.mehow.squashit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.resume

object SquashItLogger {
  private const val Capacity = 2_000
  private val logEntries = ArrayDeque<LogEntry>(Capacity)
  private val fileNameFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'log'", Locale.US)
  private val executor = Executors.newSingleThreadExecutor()
  private val mainThreadHandler = Handler(Looper.getMainLooper())

  @JvmStatic fun log(priority: Int, tag: String, message: String) {
    addLogEntry(LogEntry(priority, tag, message))
  }

  private fun addLogEntry(entry: LogEntry) = synchronized(this) {
    if (logEntries.size == Capacity) logEntries.remove()
    logEntries.add(entry)
  }

  internal suspend fun createLogFile(context: Context): File? {
    return suspendCancellableCoroutine { continuation ->
      createLogFile(context) { continuation.resume(it) }
    }
  }

  @Suppress("LongMethod")
  private fun createLogFile(context: Context, onCreated: (File?) -> Unit) {
    fun sendLogFile(file: File?) = mainThreadHandler.post {
      onCreated(file)
    }
    executor.submit {
      val entries = logEntries.toList()
      if (entries.isEmpty()) {
        sendLogFile(null)
        return@submit
      }

      val dir = context.logDirectory
      if (dir == null) {
        sendLogFile(null)
        return@submit
      }
      val output = File(dir, fileNameFormatter.format(Date()))

      val longestTagLength = entries.map { it.tag.length }.max()!!
      try {
        output.sink().buffer().use { sink ->
          for (entry in entries) {
            val log = entry.print(longestTagLength)
            sink.writeUtf8(log).writeNewLine()
          }
        }
        sendLogFile(output)
      } catch (_: IOException) {
        sendLogFile(null)
      }
    }
  }

  internal fun cleanUp(context: Context) {
    val dir = context.logDirectory ?: return
    for (file in dir.listFiles()!!) {
      if (file.extension == "log") file.delete()
    }
  }

  private val Context.logDirectory: File?
    get() {
      val externalDir = getExternalFilesDir(null) ?: return null
      val logDir = File(externalDir, "/squash-it/logs")
      logDir.mkdirs()
      return logDir
    }

  private fun BufferedSink.writeNewLine() = writeByte(10)

  private class LogEntry(
    val priority: Int,
    val tag: String,
    val message: String
  ) {
    fun print(tagPaddedLength: Int): String {
      val formattedPriority = priority.toLogLevel()
      val formattedMessage = message.replace("\\n", "\n${" ".repeat(tagPaddedLength)}")
      return "%${tagPaddedLength}s %s %s".format(tag, formattedPriority, formattedMessage)
    }

    private fun Int.toLogLevel() = when (this) {
      Log.VERBOSE -> "V"
      Log.DEBUG -> "D"
      Log.INFO -> "I"
      Log.WARN -> "W"
      Log.ERROR -> "E"
      Log.ASSERT -> "A"
      else -> "?"
    }
  }
}
