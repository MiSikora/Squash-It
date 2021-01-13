package io.mehow.squashit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import okio.BufferedSink
import okio.IOException
import okio.buffer
import okio.sink
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SquashItLogger {
  private var Capacity = 2_000
  private var LogEntries = ArrayDeque<LogEntry>(Capacity)
  private val FileNameFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'log'", Locale.US)
  private val MainThreadHandler = Handler(Looper.getMainLooper())

  @JvmStatic fun log(priority: Int, tag: String, message: String) {
    addLogEntry(LogEntry(priority, tag, message))
  }

  private fun addLogEntry(entry: LogEntry) = synchronized(this) {
    if (LogEntries.size == Capacity) LogEntries.remove()
    LogEntries.add(entry)
  }

  internal fun setLogsCapacity(capacity: Int) = synchronized(this) {
    this.Capacity = capacity
    val newQueue = ArrayDeque<LogEntry>(capacity)
    newQueue.addAll(LogEntries.take(capacity))
    LogEntries = newQueue
  }

  internal suspend fun createLogFile(context: Context): File? {
    return suspendCoroutine { continuation ->
      createLogFile(context) { continuation.resume(it) }
    }
  }

  @Suppress("LongMethod")
  private fun createLogFile(context: Context, onCreated: (File?) -> Unit) {
    fun sendLogFile(file: File?) = MainThreadHandler.post {
      onCreated(file)
    }

    val entries = LogEntries.toList()
    if (entries.isEmpty()) {
      sendLogFile(null)
      return
    }

    val dir = context.logDirectory
    if (dir == null) {
      sendLogFile(null)
      return
    }
    val output = File(dir, FileNameFormatter.format(Date()))

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

  internal fun cleanUp(context: Context) {
    val dir = context.logDirectory ?: return
    for (file in dir.listFiles()!!) {
      if (file.extension == "log") file.delete()
    }
  }

  @Suppress("ObjectPropertyNaming")
  private val Context.logDirectory: File?
    get() {
      val externalDir = getExternalFilesDir(null) ?: return null
      val logDir = File(externalDir, "/squash-it/logs")
      logDir.mkdirs()
      return logDir
    }

  private fun BufferedSink.writeNewLine() = writeByte(10)

  private class LogEntry(val priority: Int, val tag: String, val message: String) {
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
