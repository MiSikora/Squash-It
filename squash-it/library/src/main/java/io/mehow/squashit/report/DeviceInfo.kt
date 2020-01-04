package io.mehow.squashit.report

import android.os.Parcel
import android.os.Parcelable
import io.mehow.squashit.report.DeviceInfo.DateParceler
import io.mehow.squashit.report.DeviceInfo.LocaleParceler
import io.mehow.squashit.report.DeviceInfo.TimeZoneParceler
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Parcelize
@TypeParceler<Locale, LocaleParceler>
@TypeParceler<TimeZone, TimeZoneParceler>
@TypeParceler<Date, DateParceler>
internal data class DeviceInfo(
  val manufacturer: String,
  val model: String,
  val resolution: String,
  val density: String,
  val locales: List<Locale>,
  val createdAt: Date,
  val timeZone: TimeZone
) : Describable, Parcelable {
  @IgnoredOnParcel
  private val timeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).apply {
    timeZone = this@DeviceInfo.timeZone
  }

  override fun describe(): String {
    return """
      |{panel:title=Device info}
      |Manufacturer: $manufacturer
      |Model: $model
      |Resolution: $resolution
      |Density: $density
      |Locales: ${locales.joinToString(prefix = "[", postfix = "]")}
      |Local date: ${timeFormatter.format(createdAt)}
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

  object DateParceler : Parceler<Date> {
    override fun create(parcel: Parcel): Date {
      return Date(parcel.readLong())
    }

    override fun Date.write(parcel: Parcel, flags: Int) {
      parcel.writeLong(time)
    }
  }
}
