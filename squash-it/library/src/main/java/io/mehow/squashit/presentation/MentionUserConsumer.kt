package io.mehow.squashit.presentation

import io.mehow.squashit.Mentions
import io.mehow.squashit.presentation.Event.MentionUser

internal class MentionUserConsumer(
  sender: ModelSender
) : EventConsumer<MentionUser>(sender, MentionUser::class) {
  override suspend fun consume(event: MentionUser) {
    send { copy(input = input.copy(mentions = Mentions(input.mentions.users + event.user))) }
  }
}
