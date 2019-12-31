package io.mehow.screenshot

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class Dot(
  val x: Float,
  val y: Float
) : Parcelable
