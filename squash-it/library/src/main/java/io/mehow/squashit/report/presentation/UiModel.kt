package io.mehow.squashit.report.presentation

import io.mehow.squashit.report.AttachState.Unavailable
import io.mehow.squashit.report.InitState
import io.mehow.squashit.report.Mentions
import io.mehow.squashit.report.ProjectInfo
import io.mehow.squashit.report.ReportType.CreateNewIssue
import io.mehow.squashit.report.SubmitState

internal data class UiModel(
  val initState: InitState,
  val projectInfo: ProjectInfo?,
  val input: UserInput,
  val submitState: SubmitState
) {
  companion object {
    val Initial = UiModel(
        initState = InitState.Initializing,
        projectInfo = null,
        input = UserInput(
            reporter = null,
            reportType = CreateNewIssue,
            type = null,
            summary = null,
            epic = null,
            issueKey = null,
            description = null,
            mentions = Mentions(emptySet()),
            screenshotState = Unavailable,
            logsState = Unavailable,
            attachments = emptySet(),
            errors = emptySet()
        ),
        submitState = SubmitState.Idle
    )
  }
}
