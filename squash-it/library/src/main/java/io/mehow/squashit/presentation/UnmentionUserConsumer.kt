package io.mehow.squashit.presentation

import io.mehow.squashit.Mentions
import io.mehow.squashit.presentation.Event.UnmentionUser

internal class UnmentionUserConsumer(
  sender: ModelSender
) : EventConsumer<UnmentionUser>(sender, UnmentionUser::class) {
  override suspend fun consume(event: UnmentionUser) {
    send { copy(input = input.copy(mentions = Mentions(input.mentions.users - event.user))) }
  }
}
