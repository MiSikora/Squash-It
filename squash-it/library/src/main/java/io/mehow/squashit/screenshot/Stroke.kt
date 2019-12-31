package io.mehow.squashit.screenshot

import android.graphics.Path
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class Stroke(
  val dots: MutableList<Dot>,
  val brush: Brush
) : Parcelable {
  val path: Path
    get() {
      val path = Path()
      dots.forEachIndexed { index, dot ->
        if (index == 0) path.moveTo(dot.x, dot.y) else path.lineTo(dot.x, dot.y)
      }
      return path
    }
}
