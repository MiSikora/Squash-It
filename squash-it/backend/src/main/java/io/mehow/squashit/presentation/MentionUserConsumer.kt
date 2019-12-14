package io.mehow.squashit.presentation

import io.mehow.squashit.Mentions
import io.mehow.squashit.presentation.Event.MentionUser

internal class MentionUserConsumer(
  sender: ModelSender
) : EventConsumer<MentionUser>(sender, MentionUser::class) {
  override suspend fun consume(event: MentionUser) {
    send { copy(mentions = Mentions(mentions.users + event.user)) }
  }
}
