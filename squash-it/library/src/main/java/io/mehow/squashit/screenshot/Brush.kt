package io.mehow.squashit.screenshot

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class Brush(
  @Px val size: Int,
  @ColorInt val color: Int
) : Parcelable
