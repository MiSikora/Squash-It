package io.mehow.squashit.report

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal data class DeviceInfo(
  val manufacturer: String,
  val model: String,
  val supportedAbis: List<String>,
  val resolution: String,
  val density: String,
  val locales: List<Locale>,
  val createdAt: Date,
  val timeZone: TimeZone
) : Describable {
  private val timeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.US).apply {
    timeZone = this@DeviceInfo.timeZone
  }

  override fun describe(): String {
    return """
      |{panel:title=Device info}
      |Manufacturer: $manufacturer
      |Model: $model
      |Supported ABIs: ${supportedAbis.joinToString(prefix = "[", postfix = "]")}
      |Resolution: $resolution
      |Density: $density
      |Locales: ${locales.joinToString(prefix = "[", postfix = "]")}
      |Local date: ${timeFormatter.format(createdAt)}
      |Time zone: ${timeZone.getDisplayName(Locale.US)}, ${timeZone.id}
      |{panel}
    """.trimMargin()
  }
}
