package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.RemoveAttachment

internal class RemoveAttachmentConsumer(
  sender: ModelSender
) : EventConsumer<RemoveAttachment>(sender, RemoveAttachment::class) {
  override suspend fun consume(event: RemoveAttachment) {
    send { copy(attachments = attachments - event.attachment) }
  }
}
