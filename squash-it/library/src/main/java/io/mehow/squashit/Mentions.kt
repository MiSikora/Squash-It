package io.mehow.squashit

internal data class Mentions(
  val users: Set<User>
) : Describable {
  fun withUsers(vararg users: User) = copy(users = this.users + users)

  fun withoutUsers(vararg users: User) = copy(users = this.users - users)

  override fun describe(): String {
    return if (users.isEmpty()) "" else """
      |{panel:title=Mentions}
      |${users.joinToString(separator = ", ", transform = { it.mentionTag })}
      |{panel}
    """.trimMargin()
  }
}
