package io.mehow.squashit.report

internal data class Reporter(val user: User) : Describable {
  override fun describe(): String {
    return """
      |{panel:title=Reported by}
      |${user.mentionTag}
      |{panel}
    """.trimMargin()
  }
}
