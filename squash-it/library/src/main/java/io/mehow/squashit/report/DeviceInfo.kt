package io.mehow.squashit.report

import android.os.Parcel
import android.os.Parcelable
import io.mehow.squashit.report.DeviceInfo.LocaleParceler
import io.mehow.squashit.report.DeviceInfo.TimeZoneParceler
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import java.util.Locale
import java.util.TimeZone

@Parcelize
@TypeParceler<Locale, LocaleParceler>
@TypeParceler<TimeZone, TimeZoneParceler>
internal data class DeviceInfo(
  val manufacturer: String,
  val model: String,
  val resolution: String,
  val density: String,
  val locales: List<Locale>,
  val timeZone: TimeZone
) : Describable, Parcelable {
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

  object LocaleParceler : Parceler<Locale> {
    override fun create(parcel: Parcel): Locale {
      val language = parcel.readString()!!
      val country = parcel.readString()!!
      val variant = parcel.readString()!!
      return Locale(language, country, variant)
    }

    override fun Locale.write(parcel: Parcel, flags: Int) {
      parcel.writeString(language)
      parcel.writeString(country)
      parcel.writeString(variant)
    }
  }

  object TimeZoneParceler : Parceler<TimeZone> {
    override fun create(parcel: Parcel): TimeZone {
      return TimeZone.getTimeZone(parcel.readString())
    }

    override fun TimeZone.write(parcel: Parcel, flags: Int) {
      parcel.writeString(id)
    }
  }
}
