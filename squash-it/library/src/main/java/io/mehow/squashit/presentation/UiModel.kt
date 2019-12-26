package io.mehow.squashit.presentation

import io.mehow.squashit.AttachState.Unavailable
import io.mehow.squashit.InitState
import io.mehow.squashit.Mentions
import io.mehow.squashit.NewIssue
import io.mehow.squashit.ProjectInfo
import io.mehow.squashit.ReportType.CreateNewIssue
import io.mehow.squashit.SubmitState

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
            newIssue = NewIssue(
                type = null,
                summary = null,
                epic = null
            ),
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
