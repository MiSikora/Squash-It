package io.mehow.squashit.screenshot.capture

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CaptureResult(
  val code: Int,
  val data: Intent?,
  val promptShown: Boolean,
) : Parcelable
