package io.mehow.squashit.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.InputError.NoIssueId
import io.mehow.squashit.InputError.NoIssueType
import io.mehow.squashit.InputError.NoReporter
import io.mehow.squashit.InputError.ShortSummary
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.NewIssue
import io.mehow.squashit.ReportType
import io.mehow.squashit.SubmitState
import io.mehow.squashit.Summary
import io.mehow.squashit.User
import io.mehow.squashit.presentation.Event.HideError
import io.mehow.squashit.presentation.Event.SetNewIssueSummary
import io.mehow.squashit.presentation.Event.SetNewIssueType
import io.mehow.squashit.presentation.Event.SetReportType
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.Event.SetUpdateIssueKey
import io.mehow.squashit.presentation.Event.SubmitReport
import org.junit.Test

class ReportPresenterValidationTest : BaseReportPresenterTest() {
  @Test fun `invalid new issue input is detected`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem() shouldBe syncedModel.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe syncedModel.copy(
        inputErrors = setOf(NoReporter, NoIssueType, ShortSummary)
    )
  }

  @Test fun `missing reporter error can be fixed for new issue`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(HideError(NoReporter))
    var expected = syncedModel.copy(inputErrors = setOf(NoIssueType, ShortSummary))
    expectItem() shouldBe expected

    sendEvent(SetReporter(User("Bob", "123")))
    expected = expected.copy(reporter = User("Bob", "123"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `missing issue type error can be fixed`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(HideError(NoIssueType))
    var expected = syncedModel.copy(inputErrors = setOf(NoReporter, ShortSummary))
    expectItem() shouldBe expected

    sendEvent(SetNewIssueType(IssueType("ID", "Name")))
    expected = expected.copy(newIssue = NewIssue(IssueType("ID", "Name"), null, null))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `short summary error can be fixed`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(HideError(ShortSummary))
    var expected = syncedModel.copy(inputErrors = setOf(NoReporter, NoIssueType))
    expectItem() shouldBe expected

    sendEvent(SetNewIssueSummary(Summary("012345678")))
    expected = expected.copy(newIssue = NewIssue(null, Summary("012345678"), null))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expected = expected.copy(inputErrors = setOf(NoReporter, NoIssueType, ShortSummary))
    expectItem() shouldBe expected

    sendEvent(SetNewIssueSummary(Summary("0123456789")))
    expected = expected.copy(newIssue = NewIssue(null, Summary("0123456789"), null))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected.copy(inputErrors = setOf(NoReporter, NoIssueType))
  }

  @Test fun `invalid add comment input is detected`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()

    val expected = syncedModel.copy(reportType = ReportType.UpdateIssue)

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected.copy(inputErrors = setOf(NoReporter, NoIssueId))
  }

  @Test fun `missing reporter error can be fixed for add comment`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()

    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(HideError(NoReporter))
    var expected = syncedModel.copy(
        reportType = ReportType.UpdateIssue,
        inputErrors = setOf(NoIssueId)
    )
    expectItem() shouldBe expected

    sendEvent(SetReporter(User("Bob", "123")))
    expected = expected.copy(reporter = User("Bob", "123"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected
  }

  @Test fun `no issue ID error can be fixed`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()

    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    var expected = syncedModel.copy(
        reportType = ReportType.UpdateIssue,
        inputErrors = setOf(NoReporter, NoIssueId)
    )

    sendEvent(HideError(NoIssueId))
    expected = expected.copy(inputErrors = setOf(NoReporter))
    expectItem() shouldBe expected

    sendEvent(SetUpdateIssueKey(IssueKey("Key")))
    expected = expected.copy(updateIssueKey = IssueKey("Key"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected
  }
}
