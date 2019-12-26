package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.SetScreenshotState

internal class SetScreenshotStateConsumer(
  sender: ModelSender
) : EventConsumer<SetScreenshotState>(sender, SetScreenshotState::class) {
  override suspend fun consume(event: SetScreenshotState) {
    send { copy(input = input.copy(screenshotState = event.state)) }
  }
}
