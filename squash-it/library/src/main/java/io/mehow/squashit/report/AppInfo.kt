package io.mehow.squashit.report

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class AppInfo(
  val versionName: String,
  val versionCode: String,
  val packageName: String
) : Describable, Parcelable {
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
