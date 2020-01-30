package io.mehow.squashit.screenshot.capture

internal enum class TwistState {
  Start {
    override val nextState: TwistState get() = FirstBreak
    override fun proceedIf(sample: TwistSample) = sample.isFirst
  },
  FirstBreak {
    override val nextState: TwistState get() = SecondBreak
    override fun proceedIf(sample: TwistSample) = sample.isSecond
  },
  SecondBreak {
    override val nextState: TwistState get() = ThirdBreak
    override fun proceedIf(sample: TwistSample) = sample.isThird
  },
  ThirdBreak {
    override val nextState: TwistState get() = End
    override fun proceedIf(sample: TwistSample) = sample.isEnd
  },
  End {
    override val nextState: TwistState get() = Start
    override fun proceedIf(sample: TwistSample) = true
  };

  protected abstract val nextState: TwistState
  protected abstract fun proceedIf(sample: TwistSample): Boolean

  fun proceed(startTimestamp: Long, sample: TwistSample): TwistState? {
    return if (fitsTimeWindow(startTimestamp, sample)) {
      if (proceedIf(sample)) nextState else this
    } else null
  }

  private fun fitsTimeWindow(startTimestamp: Long, sample: TwistSample): Boolean {
    return sample.timestamp - startTimestamp < MeasureWindow
  }

  companion object {
    private const val MeasureWindow = 2_000_000_000
  }
}
