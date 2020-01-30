package io.mehow.squashit.screenshot.capture

internal enum class TwistState(
  private val nextStepRange: ClosedFloatingPointRange<Double>
) {
  Start(60.0..120.0) {
    override val nextState get() = FaceDown
  },
  FaceDown(150.0..180.0) {
    override val nextState get() = FaceUp
  },
  FaceUp(0.0..30.0) {
    override val nextState get() = Finish
  },
  Finish(Double.MIN_VALUE..Double.MAX_VALUE) {
    override val nextState get() = Start
  };

  protected abstract val nextState: TwistState

  fun proceed(startTimestamp: Long, sample: TwistSample): TwistState? {
    return if (fitsTimeWindow(startTimestamp, sample)) {
      if (sample in this) nextState else this
    } else null
  }

  private operator fun contains(sample: TwistSample): Boolean {
    return sample.planeOrientation in nextStepRange
  }

  private fun fitsTimeWindow(startTimestamp: Long, sample: TwistSample): Boolean {
    return sample.timestamp - startTimestamp < MeasureWindow
  }

  companion object {
    private const val MeasureWindow = 2_000_000_000
  }
}
