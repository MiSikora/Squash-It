package io.mehow.squashit

internal data class RuntimeInfo(
  val app: AppInfo,
  val device: DeviceInfo,
  val osInfo: OsInfo
) : Describable {
  override fun describe(): String {
    return """
      |${app.describe()}
      |
      |${device.describe()}
      |
      |${osInfo.describe()}
    """.trimMargin()
  }
}
