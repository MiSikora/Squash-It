package io.mehow.squashit.report

internal data class Description(
  val value: String
) : Describable {
  override fun describe(): String {
    return if (value.isEmpty()) "" else """
      |{panel:title=Reporter notes}
      |$value
      |{panel}
    """.trimMargin()
  }
}
