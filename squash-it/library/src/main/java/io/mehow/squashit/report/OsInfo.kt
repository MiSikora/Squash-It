package io.mehow.squashit.report

internal data class OsInfo(
  val release: String,
  val sdk: Int
) : Describable {
  override fun describe(): String {
    return """
      |{panel:title=OS info}
      |Release: $release
      |SDK: $sdk
      |{panel}
    """.trimMargin()
  }
}
