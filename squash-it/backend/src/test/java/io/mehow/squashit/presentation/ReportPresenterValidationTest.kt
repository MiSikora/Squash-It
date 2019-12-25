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
import io.mehow.squashit.presentation.Event.SetIssueKey
import io.mehow.squashit.presentation.Event.SetIssueType
import io.mehow.squashit.presentation.Event.SetReportType
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.Event.SetSummary
import io.mehow.squashit.presentation.Event.SubmitReport
import org.junit.Test

class ReportPresenterValidationTest : BaseReportPresenterTest() {
  @Test fun `invalid new issue input is detected`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem() shouldBe syncedModel.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe syncedModel.copy(
        errors = setOf(NoReporter, NoIssueType, ShortSummary)
    )
  }

  @Test fun `missing reporter error can be fixed for new issue`() = testPresenter {
    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    sendEvent(HideError(NoReporter))
    var expected = syncedModel.copy(errors = setOf(NoIssueType, ShortSummary))
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
    var expected = syncedModel.copy(errors = setOf(NoReporter, ShortSummary))
    expectItem() shouldBe expected

    sendEvent(SetIssueType(IssueType("ID", "Name")))
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
    var expected = syncedModel.copy(errors = setOf(NoReporter, NoIssueType))
    expectItem() shouldBe expected

    sendEvent(SetSummary(Summary("012345678")))
    expected = expected.copy(newIssue = NewIssue(null, Summary("012345678"), null))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expected = expected.copy(errors = setOf(NoReporter, NoIssueType, ShortSummary))
    expectItem() shouldBe expected

    sendEvent(SetSummary(Summary("0123456789")))
    expected = expected.copy(newIssue = NewIssue(null, Summary("0123456789"), null))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected.copy(errors = setOf(NoReporter, NoIssueType))
  }

  @Test fun `invalid add comment input is detected`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()

    val expected = syncedModel.copy(reportType = ReportType.UpdateIssue)

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected.copy(errors = setOf(NoReporter, NoIssueId))
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
        errors = setOf(NoIssueId)
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
        errors = setOf(NoReporter, NoIssueId)
    )

    sendEvent(HideError(NoIssueId))
    expected = expected.copy(errors = setOf(NoReporter))
    expectItem() shouldBe expected

    sendEvent(SetIssueKey(IssueKey("Key")))
    expected = expected.copy(issueKey = IssueKey("Key"))
    expectItem() shouldBe expected

    sendEvent(SubmitReport)
    expectItem() shouldBe expected.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe expected
  }
}
