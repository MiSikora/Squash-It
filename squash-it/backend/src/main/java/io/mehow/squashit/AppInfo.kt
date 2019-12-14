package io.mehow.squashit

data class AppInfo(
  val versionName: String,
  val versionCode: String,
  val packageName: String
) : Describable {
  override fun describe(): String {
    return """
      |{panel:title=Application info}
      |Version name: $versionName
      |Version code: $versionCode
      |Package name: $packageName
      |{panel}
    """.trimMargin()
  }
}
