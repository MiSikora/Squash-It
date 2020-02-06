package io.mehow.squashit.screenshot.capture

import io.mehow.squashit.screenshot.capture.TwistState.Start

internal class RotationSample(
  private val timestamp: Long,
  private val planeOrientation: Double
) : TwistSample {
  override fun proceed(startTimestamp: Long, state: TwistState): TwistState {
    return if (fitsTimeWindow(startTimestamp)) {
      if (this in state) state.nextState else state
    } else Start
  }

  private operator fun TwistState.contains(sample: RotationSample): Boolean {
    return sample.planeOrientation in rotationRange
  }

  private fun fitsTimeWindow(startTimestamp: Long): Boolean {
    return timestamp - startTimestamp < 2_000_000_000
  }
}
