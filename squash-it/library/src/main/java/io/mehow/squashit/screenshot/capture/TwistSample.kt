package io.mehow.squashit.screenshot.capture

internal interface TwistSample {
  fun proceed(startTimestamp: Long, state: TwistState): TwistState
}
