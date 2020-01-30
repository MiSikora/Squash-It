package io.mehow.squashit.screenshot.capture

import io.mehow.squashit.screenshot.capture.TwistPoint.End
import io.mehow.squashit.screenshot.capture.TwistPoint.FirstBreak
import io.mehow.squashit.screenshot.capture.TwistPoint.SecondBreak
import io.mehow.squashit.screenshot.capture.TwistPoint.ThirdBreak

internal class TwistSample(
  val timestamp: Long,
  val ax: Float,
  val az: Float
) {
  private val twistPoints = TwistPoint.CachedValues.filter { this in it }
  val isFirst = FirstBreak in twistPoints
  val isSecond = SecondBreak in twistPoints
  val isThird = ThirdBreak in twistPoints
  val isEnd = End in twistPoints
}
