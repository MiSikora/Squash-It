package io.mehow.squashit

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.TimeZone

@Parcelize
internal data class ParcelableDeviceInfo(
  val manufacturer: String,
  val model: String,
  val resolution: String,
  val density: String,
  val locales: List<ParcelableLocale>,
  val timeZoneId: String
) : Parcelable {
  fun toDeviceInfo(): DeviceInfo {
    return DeviceInfo(
        manufacturer,
        model,
        resolution,
        density,
        locales.map(ParcelableLocale::toLocale),
        TimeZone.getTimeZone(timeZoneId)
    )
  }
}
