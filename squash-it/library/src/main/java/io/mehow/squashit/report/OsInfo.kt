package io.mehow.squashit.report

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class OsInfo(
  val release: String,
  val sdk: Int
) : Describable, Parcelable {
  override fun describe(): String {
    return """
      |{panel:title=OS info}
      |Release: $release
      |SDK: $sdk
      |{panel}
    """.trimMargin()
  }
}
