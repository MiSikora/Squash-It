package io.mehow.squashit.presentation.extensions

import io.mehow.squashit.AttachState
import io.mehow.squashit.Attachment
import io.mehow.squashit.Description
import io.mehow.squashit.Epic
import io.mehow.squashit.InitState
import io.mehow.squashit.InputError
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.ProjectInfo
import io.mehow.squashit.ReportType
import io.mehow.squashit.SubmitState
import io.mehow.squashit.Summary
import io.mehow.squashit.User
import io.mehow.squashit.presentation.UiModel
import io.mehow.squashit.presentation.UserInput

internal fun UiModel.withInitState(state: InitState): UiModel {
  return copy(initState = state)
}

internal fun UiModel.withProjectInfo(info: ProjectInfo): UiModel {
  return copy(projectInfo = info)
}

internal fun UiModel.withProjectInfo(builder: ProjectInfo.() -> ProjectInfo): UiModel {
  return copy(projectInfo = projectInfo?.builder())
}

internal fun UiModel.withSubmitState(state: SubmitState) : UiModel {
  return copy(submitState = state)
}

internal fun UiModel.withUserInput(builder: UserInput.() -> UserInput): UiModel {
  return copy(input = input.builder())
}

internal fun UiModel.withReporter(reporter: User): UiModel {
  return withUserInput { copy(reporter = reporter) }
}

internal fun UiModel.withReportType(type: ReportType): UiModel {
  return withUserInput { copy(reportType = type) }
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
  return withUserInput { copy(issueKey = key) }
}

internal fun UiModel.withDescription(description: Description): UiModel {
  return withUserInput { copy(description = description) }
}

internal fun UiModel.withMentions(vararg users: User): UiModel {
  return withUserInput { withMentions(*users) }
}

internal fun UiModel.withoutMentions(vararg users: User): UiModel {
  return withUserInput { withoutMentions(*users) }
}

internal fun UiModel.withScreenshot(state: AttachState): UiModel {
  return withUserInput { copy(screenshotState = state) }
}

internal fun UiModel.withLogs(state: AttachState): UiModel {
  return withUserInput { copy(logsState = state) }
}

internal fun UiModel.withAttachments(vararg attachments: Attachment): UiModel {
  return withUserInput { withAttachments(*attachments) }
}

internal fun UiModel.withoutAttachments(vararg attachments: Attachment): UiModel {
  return withUserInput { withoutAttachments(*attachments) }
}

internal fun UiModel.withErrors(vararg errors: InputError): UiModel {
  return withUserInput { copy(errors = this.errors + errors) }
}

internal fun UiModel.withoutError(error: InputError): UiModel {
  return withUserInput { withoutError(error) }
}
