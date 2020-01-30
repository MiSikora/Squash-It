package io.mehow.squashit.screenshot.capture

import io.mehow.squashit.screenshot.capture.TwistState.End
import io.mehow.squashit.screenshot.capture.TwistState.Middle

internal class TwistSample(
  val timestamp: Long,
  val ax: Float,
  val ay: Float,
  val az: Float
) {
  private val twistPoints = TwistState.CachedValues.filter { this in it }
  val magnitude = ax * ax + ay * ay + az * az
  val isMiddle = Middle in twistPoints
  val isEnd = End in twistPoints
}
