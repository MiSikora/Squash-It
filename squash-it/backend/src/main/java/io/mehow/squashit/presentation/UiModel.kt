package io.mehow.squashit.presentation

import io.mehow.squashit.AttachState
import io.mehow.squashit.AttachState.Unavailable
import io.mehow.squashit.AttachmentItem
import io.mehow.squashit.Description
import io.mehow.squashit.InitState
import io.mehow.squashit.InputError
import io.mehow.squashit.IssueKey
import io.mehow.squashit.Mentions
import io.mehow.squashit.NewIssue
import io.mehow.squashit.ProjectInfo
import io.mehow.squashit.ReportType
import io.mehow.squashit.ReportType.CreateNewIssue
import io.mehow.squashit.SubmitState
import io.mehow.squashit.User

data class UiModel(
  val initState: InitState,
  val projectInfo: ProjectInfo?,
  val reporter: User?,
  val reportType: ReportType,
  val newIssue: NewIssue,
  val updateIssueKey: IssueKey?,
  val issueDescription: Description?,
  val mentions: Mentions,
  val screenshotState: AttachState,
  val logsState: AttachState,
  val customAttachments: Set<AttachmentItem>,
  val inputErrors: Set<InputError>,
  val submitState: SubmitState
) {
  companion object {
    val Initial = UiModel(
        initState = InitState.Initializing,
        projectInfo = null,
        reporter = null,
        reportType = CreateNewIssue,
        newIssue = NewIssue(
            type = null,
            summary = null,
            epic = null
        ),
        updateIssueKey = null,
        issueDescription = null,
        mentions = Mentions(emptySet()),
        screenshotState = Unavailable,
        logsState = Unavailable,
        customAttachments = emptySet(),
        inputErrors = emptySet(),
        submitState = SubmitState.Idle
    )
  }
}
