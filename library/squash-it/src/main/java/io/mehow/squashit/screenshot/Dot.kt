package io.mehow.squashit.screenshot

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Dot(val x: Float, val y: Float) : Parcelable
