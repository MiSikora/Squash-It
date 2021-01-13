package io.mehow.squashit.report

import com.squareup.moshi.Moshi
import okio.buffer
import okio.sink
import okio.source
import java.io.File

internal class ProjectInfoStore(private val storageDir: File, moshi: Moshi) {
  private val adapter = moshi.adapter(ProjectInfo::class.java)
  private var cachedInfo: ProjectInfo? = null

  fun save(info: ProjectInfo) = synchronized<Unit>(this) {
    cachedInfo = info
    projectInfoFile().sink().buffer().use { sink ->
      sink.writeUtf8(adapter.toJson(info))
    }
  }

  fun read() = synchronized(this) {
    if (cachedInfo != null) cachedInfo
    val file = projectInfoFile()
    if (!file.exists()) return@synchronized null
    val info = adapter.fromJson(file.source().buffer())
    cachedInfo = info
    return@synchronized info
  }

  private fun projectInfoFile(): File {
    val infoDir = File(storageDir, "squashIt")
    infoDir.mkdirs()
    return File(infoDir, "project-info.json")
  }
}
