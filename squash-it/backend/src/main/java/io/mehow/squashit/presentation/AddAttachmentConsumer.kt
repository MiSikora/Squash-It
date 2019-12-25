package io.mehow.squashit.presentation

import io.mehow.squashit.presentation.Event.AddAttachment

internal class AddAttachmentConsumer(
  sender: ModelSender
) : EventConsumer<AddAttachment>(sender, AddAttachment::class) {
  override suspend fun consume(event: AddAttachment) {
    send { copy(attachments = attachments + event.attachment) }
  }
}
