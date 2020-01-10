package io.mehow.squashit

data class UiModel(val credentials: List<Credentials>, val state: ActionState) {
  data class Accumulator(val func: (UiModel) -> UiModel)
}
