package io.mehow.squashit

data class RuntimeInfo(
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
