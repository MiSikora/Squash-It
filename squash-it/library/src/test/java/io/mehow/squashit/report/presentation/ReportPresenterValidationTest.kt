package io.mehow.squashit.report.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.report.InputError.NoIssueId
import io.mehow.squashit.report.InputError.NoIssueType
import io.mehow.squashit.report.InputError.NoReporter
import io.mehow.squashit.report.InputError.ShortSummary
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.User
import io.mehow.squashit.report.presentation.Event.SubmitReport
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.extensions.withErrors
import io.mehow.squashit.report.presentation.extensions.withIssueKey
import io.mehow.squashit.report.presentation.extensions.withIssueType
import io.mehow.squashit.report.presentation.extensions.withReportType
import io.mehow.squashit.report.presentation.extensions.withReporter
import io.mehow.squashit.report.presentation.extensions.withSubmitState
import io.mehow.squashit.report.presentation.extensions.withSummary
import io.mehow.squashit.report.presentation.extensions.withoutError
import org.junit.Test

internal class ReportPresenterValidationTest : BaseReportPresenterTest() {
  @Test fun `invalid new issue input is detected`() = testPresenter {
    presenter.sendEvent(SubmitReport(syncedModel.input))
    expectItem() shouldBe syncedModel.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe syncedModel.withErrors(NoReporter, NoIssueType, ShortSummary)
  }

  @Test fun `missing reporter error can be fixed for new issue`() = testPresenter {
    presenter.sendEvent(SubmitReport(syncedModel.input))
    expectItem()
    expectItem()

    presenter.sendEvent(UpdateInput.hideError(NoReporter))
    var expected = syncedModel.withErrors(NoIssueType, ShortSummary)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.reporter(User("Bob", "123")))
    expected = expected.withReporter(User("Bob", "123"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `missing issue type error can be fixed for new issue`() = testPresenter {
    presenter.sendEvent(SubmitReport(syncedModel.input))
    expectItem()
    expectItem()

    presenter.sendEvent(UpdateInput.hideError(NoIssueType))
    var expected = syncedModel.withErrors(NoReporter, ShortSummary)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.issueType(IssueType("ID", "Name")))
    expected = expected.withIssueType(IssueType("ID", "Name"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `short summary error can be fixed for new issue`() = testPresenter {
    presenter.sendEvent(SubmitReport(syncedModel.input))
    expectItem()
    expectItem()

    presenter.sendEvent(UpdateInput.hideError(ShortSummary))
    var expected = syncedModel.withErrors(NoReporter, NoIssueType)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.summary(Summary("012345678")))
    expected = expected.withSummary(Summary("012345678"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expected = expected.withErrors(NoReporter, NoIssueType, ShortSummary)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.summary(Summary("0123456789")))
    expected = expected.withSummary(Summary("0123456789"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected.withoutError(ShortSummary)
  }

  @Test fun `invalid add comment input is detected`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddCommentToIssue))
    expectItem()

    val expected = syncedModel.withReportType(ReportType.AddCommentToIssue)

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected.withErrors(NoReporter, NoIssueId)
  }

  @Test fun `missing reporter error can be fixed for add comment`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddCommentToIssue))
    expectItem()

    presenter.sendEvent(SubmitReport(syncedModel.input.withReportType(ReportType.AddCommentToIssue)))
    expectItem()
    expectItem()

    presenter.sendEvent(UpdateInput.hideError(NoReporter))
    var expected = syncedModel
        .withReportType(ReportType.AddCommentToIssue)
        .withErrors(NoIssueId)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.reporter(User("Bob", "123")))
    expected = expected.withReporter(User("Bob", "123"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `no issue ID error can be fixed for add comment`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddCommentToIssue))
    expectItem()

    presenter.sendEvent(SubmitReport(syncedModel.input.withReportType(ReportType.AddCommentToIssue)))
    expectItem()
    expectItem()

    var expected = syncedModel
        .withReportType(ReportType.AddCommentToIssue)
        .withErrors(NoReporter, NoIssueId)

    presenter.sendEvent(UpdateInput.hideError(NoIssueId))
    expected = expected.withoutError(NoIssueId)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.issueKey(IssueKey("Key")))
    expected = expected.withIssueKey(IssueKey("Key"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `invalid sub task input is detected`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddSubTaskToIssue))
    expectItem()

    val expected = syncedModel.withReportType(ReportType.AddSubTaskToIssue)

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected.withErrors(NoReporter, NoIssueId, ShortSummary)
  }

  @Test fun `missing reporter error can be fixed for create sub task`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddSubTaskToIssue))
    expectItem()

    presenter.sendEvent(SubmitReport(syncedModel.input.withReportType(ReportType.AddSubTaskToIssue)))
    expectItem()
    expectItem()

    presenter.sendEvent(UpdateInput.hideError(NoReporter))
    var expected = syncedModel
        .withReportType(ReportType.AddSubTaskToIssue)
        .withErrors(NoIssueId, ShortSummary)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.reporter(User("Bob", "123")))
    expected = expected.withReporter(User("Bob", "123"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `no issue ID error can be fixed for create sub task`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddSubTaskToIssue))
    expectItem()

    presenter.sendEvent(SubmitReport(syncedModel.input.withReportType(ReportType.AddSubTaskToIssue)))
    expectItem()
    expectItem()

    var expected = syncedModel
        .withReportType(ReportType.AddSubTaskToIssue)
        .withErrors(NoReporter, NoIssueId, ShortSummary)

    presenter.sendEvent(UpdateInput.hideError(NoIssueId))
    expected = expected.withoutError(NoIssueId)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.issueKey(IssueKey("Key")))
    expected = expected.withIssueKey(IssueKey("Key"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `short summary error can be fixed for create sub task`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddSubTaskToIssue))
    expectItem()

    presenter.sendEvent(SubmitReport(syncedModel.input.withReportType(ReportType.AddSubTaskToIssue)))
    expectItem()
    expectItem()

    var expected = syncedModel.withReportType(ReportType.AddSubTaskToIssue)

    presenter.sendEvent(UpdateInput.hideError(ShortSummary))
    expected = expected.withErrors(NoReporter, NoIssueId)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.summary(Summary("012345678")))
    expected = expected.withSummary(Summary("012345678"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expected = expected.withErrors(NoReporter, NoIssueId, ShortSummary)
    expectItem() shouldBe expected

    presenter.sendEvent(UpdateInput.summary(Summary("0123456789")))
    expected = expected.withSummary(Summary("0123456789"))
    expectItem() shouldBe expected

    presenter.sendEvent(SubmitReport(expected.input))
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected.withoutError(ShortSummary)
  }
}
