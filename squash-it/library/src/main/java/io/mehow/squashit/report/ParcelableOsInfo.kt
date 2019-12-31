package io.mehow.squashit.report

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ParcelableOsInfo(
  val release: String,
  val sdk: Int
) : Parcelable {
  fun toOsInfo(): OsInfo {
    return OsInfo(release, sdk)
  }
}
