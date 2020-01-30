package io.mehow.squashit.screenshot.capture

internal enum class TwistState(
  private val az: ClosedFloatingPointRange<Float>
) {
  Start(0.7f..1.3f) {
    override val nextState: TwistState get() = Middle
    override fun proceedIf(sample: TwistSample) = sample.isMiddle
  },
  Middle(-1.3f..-0.7f) {
    override val nextState: TwistState get() = End
    override fun proceedIf(sample: TwistSample) = sample.isEnd
  },
  End(0.7f..1.3f) {
    override val nextState: TwistState get() = Start
    override fun proceedIf(sample: TwistSample) = true
  };

  protected abstract val nextState: TwistState
  protected abstract fun proceedIf(sample: TwistSample): Boolean

  fun proceed(startTimestamp: Long, sample: TwistSample): TwistState? {
    return if (fitsTimeWindow(startTimestamp, sample) && sample.magnitude < AccelerationThreshold) {
      if (proceedIf(sample)) nextState else this
    } else null
  }

  operator fun contains(sample: TwistSample): Boolean {
    return sample.az in az
  }

  private fun fitsTimeWindow(startTimestamp: Long, sample: TwistSample): Boolean {
    return sample.timestamp - startTimestamp < MeasureWindow
  }

  companion object {
    val CachedValues = values()
    private const val AccelerationThreshold = 1.44
    private const val MeasureWindow = 1_000_000_000
  }
}
