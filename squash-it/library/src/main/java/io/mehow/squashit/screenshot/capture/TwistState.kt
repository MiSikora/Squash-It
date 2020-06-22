package io.mehow.squashit.screenshot.capture

internal enum class TwistState(
  val rotationRange: ClosedFloatingPointRange<Double>,
  val accelerationRange: ClosedFloatingPointRange<Float>
) {
  Start(
    rotationRange = 60.0..120.0,
    accelerationRange = 0.6f..0.8f
  ) {
    override val nextState get() = FaceDown
  },
  FaceDown(
    rotationRange = 150.0..180.0,
    accelerationRange = -1.1f..-0.9f
  ) {
    override val nextState get() = FaceUp
  },
  FaceUp(
    rotationRange = 0.0..30.0,
    accelerationRange = 0.9f..1.1f
  ) {
    override val nextState get() = Finish
  },
  Finish(
    rotationRange = -Double.MAX_VALUE..Double.MAX_VALUE,
    accelerationRange = -Float.MAX_VALUE..Float.MAX_VALUE
  ) {
    override val nextState get() = Start
  };

  abstract val nextState: TwistState
}
