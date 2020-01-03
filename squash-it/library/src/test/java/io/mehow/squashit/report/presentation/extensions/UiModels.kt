package io.mehow.squashit.report.presentation.extensions

import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.Attachment
import io.mehow.squashit.report.AttachmentId
import io.mehow.squashit.report.Description
import io.mehow.squashit.report.Epic
import io.mehow.squashit.report.InitState
import io.mehow.squashit.report.InputError
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.ProjectInfo
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.User
import io.mehow.squashit.report.presentation.UiModel
import io.mehow.squashit.report.presentation.UserInput

internal fun UiModel.withInitState(state: InitState): UiModel {
  return copy(initState = state)
}

internal fun UiModel.withProjectInfo(info: ProjectInfo): UiModel {
  return copy(projectInfo = info)
}

internal fun UiModel.withProjectInfo(builder: ProjectInfo.() -> ProjectInfo): UiModel {
  return copy(projectInfo = projectInfo?.builder())
}

internal fun UiModel.withSubmitState(state: SubmitState): UiModel {
  return copy(submitState = state)
}

internal fun UiModel.withUserInput(builder: UserInput.() -> UserInput): UiModel {
  return copy(input = input.builder())
}

internal fun UiModel.withReporter(reporter: User): UiModel {
  return withUserInput { withReporter(reporter) }
}

internal fun UiModel.withReportType(type: ReportType): UiModel {
  return withUserInput { withReportType(type) }
}

internal fun UiModel.withNewIssueType(type: IssueType): UiModel {
  return withUserInput { withNewIssueType(type) }
}

internal fun UiModel.withNewIssueSummary(summary: Summary): UiModel {
  return withUserInput { withNewIssueSummary(summary) }
}

internal fun UiModel.withNewIssueEpic(epic: Epic): UiModel {
  return withUserInput { withNewIssueEpic(epic) }
}

internal fun UiModel.withIssueKey(key: IssueKey): UiModel {
  return withUserInput { withIssueKey(key) }
}

internal fun UiModel.withDescription(description: Description): UiModel {
  return withUserInput { withDescription(description) }
}

internal fun UiModel.withMentions(vararg users: User): UiModel {
  return withUserInput { withMentions(*users) }
}

internal fun UiModel.withoutMentions(vararg users: User): UiModel {
  return withUserInput { withoutMentions(*users) }
}

internal fun UiModel.withScreenshot(state: AttachState): UiModel {
  return withUserInput { withScreenshot(state) }
}

internal fun UiModel.withLogs(state: AttachState): UiModel {
  return withUserInput { withLogs(state) }
}

internal fun UiModel.withAttachments(vararg attachments: Attachment): UiModel {
  return withUserInput { withAttachments(*attachments) }
}

internal fun UiModel.withoutAttachments(vararg ids: AttachmentId): UiModel {
  return withUserInput { withoutAttachments(*ids) }
}

internal fun UiModel.withErrors(vararg errors: InputError): UiModel {
  return withUserInput { copy(errors = this.errors + errors) }
}

internal fun UiModel.withoutError(error: InputError): UiModel {
  return withUserInput { withoutError(error) }
}
