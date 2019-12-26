package io.mehow.squashit

import java.util.Locale
import java.util.TimeZone

internal data class DeviceInfo(
  val manufacturer: String,
  val model: String,
  val resolution: String,
  val density: String,
  val locales: List<Locale>,
  val timeZone: TimeZone
) : Describable {
  override fun describe(): String {
    return """
      |{panel:title=Device info}
      |Manufacturer: $manufacturer
      |Model: $model
      |Resolution: $resolution
      |Density: $density
      |Locales: ${locales.joinToString(prefix = "[", postfix = "]")}
      |Time zone: ${timeZone.displayName}, ${timeZone.id}
      |{panel}
    """.trimMargin()
  }
}
