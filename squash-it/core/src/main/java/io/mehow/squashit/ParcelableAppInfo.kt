package io.mehow.squashit

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ParcelableAppInfo(
  val versionName: String,
  val versionCode: String,
  val packageName: String
) : Parcelable {
  fun toAppInfo(): AppInfo {
    return AppInfo(versionName, versionCode, packageName)
  }
}
