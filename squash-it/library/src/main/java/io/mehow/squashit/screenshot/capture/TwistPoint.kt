package io.mehow.squashit.screenshot.capture

internal enum class TwistPoint(
  private val leftAx: ClosedFloatingPointRange<Float>,
  private val leftAz: ClosedFloatingPointRange<Float>,
  private val rightAx: ClosedFloatingPointRange<Float>,
  private val rightAz: ClosedFloatingPointRange<Float>
) {
  Start(
      leftAx = -0.25f..0.1f,
      leftAz = 0.8f..1.2f,
      rightAx = -0.1f..0.25f,
      rightAz = 0.8f..1.2f
  ),
  FirstBreak(
      leftAx = -1.1f..-0.4f,
      leftAz = -0.3f..0.3f,
      rightAx = 0.5f..1.2f,
      rightAz = -0.3f..0.3f
  ),
  SecondBreak(
      leftAx = -0.25f..0.25f,
      leftAz = -1.5f..-0.8f,
      rightAx = -0.25f..0.25f,
      rightAz = -1.5f..-0.8f
  ),
  ThirdBreak(
      leftAx = -1.1f..-0.4f,
      leftAz = -0.3f..0.3f,
      rightAx = 0.5f..1.2f,
      rightAz = -0.3f..0.3f
  ),
  End(
      leftAx = -0.25f..0.1f,
      leftAz = 0.8f..1.2f,
      rightAx = -0.1f..0.25f,
      rightAz = 0.8f..1.2f
  );

  operator fun contains(sample: TwistSample): Boolean {
    return isCorrectRightTwistFootprint(sample) || isCorrectLeftTwistFootprint(sample)
  }

  private fun isCorrectRightTwistFootprint(sample: TwistSample): Boolean {
    return sample.ax in rightAx && sample.az in rightAz
  }

  private fun isCorrectLeftTwistFootprint(sample: TwistSample): Boolean {
    return sample.ax in leftAx && sample.az in leftAz
  }

  companion object {
    val CachedValues = values()
  }
}
