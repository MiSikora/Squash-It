package io.mehow.squashit

internal sealed class ReportAttempt {
  data class Valid(val report: Report) : ReportAttempt()
  data class Invalid(val errors: Set<InputError>) : ReportAttempt()
}
