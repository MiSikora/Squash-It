package io.mehow.squashit.screenshot.capture

import io.mehow.squashit.screenshot.capture.TwistState.Start

internal class AccelerationSample(
  private val timestamp: Long,
  private val az: Float
) : TwistSample {
  override fun proceed(startTimestamp: Long, state: TwistState): TwistState {
    return if (fitsTimeWindow(startTimestamp)) {
      if (this in state) state.nextState else state
    } else Start
  }

  private operator fun TwistState.contains(sample: AccelerationSample): Boolean {
    return sample.az in accelerationRange
  }

  private fun fitsTimeWindow(startTimestamp: Long): Boolean {
    return timestamp - startTimestamp < 2_000_000_000
  }
}
