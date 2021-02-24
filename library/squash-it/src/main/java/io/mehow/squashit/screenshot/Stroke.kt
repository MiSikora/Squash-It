package io.mehow.squashit.screenshot

import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.graphics.Paint.Style.STROKE
import android.graphics.Path
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Stroke(val dots: MutableList<Dot>, val brush: Brush) : Parcelable {
  val path: Path
    get() {
      val path = Path()
      dots.forEachIndexed { index, dot ->
        if (index == 0) path.moveTo(dot.x, dot.y) else path.lineTo(dot.x, dot.y)
      }
      return path
    }

  val paint: Paint
    get() {
      return Paint().apply {
        isAntiAlias = true
        style = STROKE
        strokeJoin = Join.ROUND
        strokeCap = Cap.ROUND
        color = brush.color
        strokeWidth = brush.size.toFloat()
      }
    }
}
