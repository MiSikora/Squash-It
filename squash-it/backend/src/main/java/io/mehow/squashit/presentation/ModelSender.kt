package io.mehow.squashit.presentation

internal class ModelSender(
  private var model: UiModel,
  private val sendModel: suspend (model: UiModel) -> Unit
) {
  suspend fun send(builder: suspend UiModel.() -> UiModel) {
    val newModel = model.builder()
    if (model == newModel) return
    model = newModel
    sendModel(newModel)
  }
}
