package io.mehow.squashit

sealed class ActionState {
  object Idle : ActionState()
  data class Added(val credentials: Credentials) : ActionState()
  data class Updated(val credentials: Credentials) : ActionState()
  data class Deleted(val credentials: Credentials) : ActionState()
}
