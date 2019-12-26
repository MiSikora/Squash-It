package io.mehow.squashit.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.InputError.NoIssueId
import io.mehow.squashit.InputError.NoIssueType
import io.mehow.squashit.InputError.NoReporter
import io.mehow.squashit.InputError.ShortSummary
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.ReportType
import io.mehow.squashit.SubmitState
import io.mehow.squashit.Summary
import io.mehow.squashit.User
import io.mehow.squashit.presentation.Event.DismissError
import io.mehow.squashit.presentation.Event.SetIssueKey
import io.mehow.squashit.presentation.Event.SetIssueType
import io.mehow.squashit.presentation.Event.SetReportType
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.Event.SetSummary
import io.mehow.squashit.presentation.Event.SubmitReport
import io.mehow.squashit.presentation.extensions.withErrors
import io.mehow.squashit.presentation.extensions.withIssueKey
import io.mehow.squashit.presentation.extensions.withNewIssueSummary
import io.mehow.squashit.presentation.extensions.withNewIssueType
import io.mehow.squashit.presentation.extensions.withReportType
import io.mehow.squashit.presentation.extensions.withReporter
import io.mehow.squashit.presentation.extensions.withSubmitState
import io.mehow.squashit.presentation.extensions.withoutError
import org.junit.Test

internal class ReportPresenterValidationTest : BaseReportPresenterTest() {
  @Test fun `invalid new issue input is detected`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem() shouldBe syncedModel.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe syncedModel.withErrors(NoReporter, NoIssueType, ShortSummary)
  }

  @Test fun `missing reporter error can be fixed for new issue`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(DismissError(NoReporter))
    var expected = syncedModel.withErrors(NoIssueType, ShortSummary)
    expectItem() shouldBe expected

    sendEvent(SetReporter(User("Bob", "123")))
    expected = expected.withReporter(User("Bob", "123"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `missing issue type error can be fixed`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(DismissError(NoIssueType))
    var expected = syncedModel.withErrors(NoReporter, ShortSummary)
    expectItem() shouldBe expected

    sendEvent(SetIssueType(IssueType("ID", "Name")))
    expected = expected.withNewIssueType(IssueType("ID", "Name"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `short summary error can be fixed`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(DismissError(ShortSummary))
    var expected = syncedModel.withErrors(NoReporter, NoIssueType)
    expectItem() shouldBe expected

    sendEvent(SetSummary(Summary("012345678")))
    expected = expected.withNewIssueSummary(Summary("012345678"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expected = expected.withErrors(NoReporter, NoIssueType, ShortSummary)
    expectItem() shouldBe expected

    sendEvent(SetSummary(Summary("0123456789")))
    expected = expected.withNewIssueSummary(Summary("0123456789"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected.withoutError(ShortSummary)
  }

  @Test fun `invalid add comment input is detected`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()

    val expected = syncedModel.withReportType(ReportType.UpdateIssue)

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected.withErrors(NoReporter, NoIssueId)
  }

  @Test fun `missing reporter error can be fixed for add comment`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()

    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(DismissError(NoReporter))
    var expected = syncedModel
        .withReportType(ReportType.UpdateIssue)
        .withErrors(NoIssueId)
    expectItem() shouldBe expected

    sendEvent(SetReporter(User("Bob", "123")))
    expected = expected.withReporter(User("Bob", "123"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `no issue ID error can be fixed`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()

    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    var expected = syncedModel
        .withReportType(ReportType.UpdateIssue)
        .withErrors(NoReporter, NoIssueId)

    sendEvent(DismissError(NoIssueId))
    expected = expected.withoutError(NoIssueId)
    expectItem() shouldBe expected

    sendEvent(SetIssueKey(IssueKey("Key")))
    expected = expected.withIssueKey(IssueKey("Key"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe expected
  }
}
