package io.mehow.squashit.report.presentation

import io.kotest.matchers.shouldBe
import io.mehow.squashit.FlowAssert
import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.Description
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.Mentions
import io.mehow.squashit.report.Report
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.User
import io.mehow.squashit.report.presentation.Event.Reattach
import io.mehow.squashit.report.presentation.Event.RetrySubmission
import io.mehow.squashit.report.presentation.Event.SubmitReport
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.extensions.withDescription
import io.mehow.squashit.report.presentation.extensions.withIssueKey
import io.mehow.squashit.report.presentation.extensions.withLogs
import io.mehow.squashit.report.presentation.extensions.withMentions
import io.mehow.squashit.report.presentation.extensions.withReportType
import io.mehow.squashit.report.presentation.extensions.withReporter
import io.mehow.squashit.report.presentation.extensions.withSubmitState
import io.mehow.squashit.report.presentation.extensions.withSummary
import org.junit.Test

internal class ReportPresenterSubmitSubTaskTest : BaseReportPresenterTest() {
  @Test fun `sub task can be created from UI model`() = testNewIssueReport {
    presenter.sendEvent(SubmitReport(createSubTaskModel.input))
    expectItem() shouldBe createSubTaskModel.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe createSubTaskModel.withSubmitState(
      SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `sub task creation failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    presenter.sendEvent(SubmitReport(createSubTaskModel.input))
    expectItem()
    expectItem() shouldBe createSubTaskModel.withSubmitState(
      SubmitState.Failed(createSubTaskReport)
    )
  }

  @Test fun `sub task creation can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    presenter.sendEvent(SubmitReport(createSubTaskModel.input))
    expectItem()
    expectItem()

    presenterFactory.jiraApi.newIssueFactory.disableErrors()

    presenter.sendEvent(RetrySubmission(createSubTaskReport))
    expectItem() shouldBe createSubTaskModel.withSubmitState(SubmitState.Resubmitting)
    expectItem() shouldBe createSubTaskModel.withSubmitState(
      SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `add attachments failure does not matter without any attachments`() =
    testNewIssueReport {
      presenterFactory.jiraApi.attachmentsFactory.enableErrors()

      presenter.sendEvent(SubmitReport(createSubTaskModel.input))
      expectItem()
      expectItem() shouldBe createSubTaskModel.withSubmitState(
        SubmitState.Submitted(IssueKey("Issue ID"))
      )
    }

  @Test fun `add attachments failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    val model = createSubTaskModel.withLogs(AttachState.Attach(logsFile))
    val attachments = setOf(model.input.logsState.body!!)
    presenter.sendEvent(UpdateInput.logs(AttachState.Attach(logsFile)))
    expectItem()

    presenter.sendEvent(
      SubmitReport(createSubTaskModel.input.withLogs(AttachState.Attach(logsFile)))
    )
    expectItem()
    expectItem() shouldBe model.withSubmitState(
      SubmitState.FailedToAttach(IssueKey("Issue ID"), attachments)
    )
  }

  @Test fun `adding attachments can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    presenter.sendEvent(UpdateInput.logs(AttachState.Attach(logsFile)))
    expectItem()

    presenter.sendEvent(
      SubmitReport(createSubTaskModel.input.withLogs(AttachState.Attach(logsFile)))
    )
    expectItem()
    expectItem()

    val model = createSubTaskModel.withLogs(AttachState.Attach(logsFile))
    val attachments = setOf(model.input.logsState.body!!)

    presenterFactory.jiraApi.attachmentsFactory.disableErrors()
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    presenter.sendEvent(Reattach(IssueKey("Issue ID"), attachments))
    expectItem() shouldBe model.withSubmitState(SubmitState.Reattaching)
    expectItem() shouldBe model.withSubmitState(SubmitState.AddedAttachments(IssueKey("Issue ID")))
  }

  private val createSubTaskModel = syncedModel
    .withReportType(ReportType.AddSubTaskToIssue)
    .withReporter(User("Reporter Name", "Reporter ID"))
    .withIssueKey(IssueKey("Issue ID"))
    .withDescription(Description("Description"))
    .withMentions(User("Mention Name", "Mention ID"))
    .withSummary(Summary("Valid Summary"))

  private val createSubTaskReport = Report.AddSubTask(
    reporter = User("Reporter Name", "Reporter ID"),
    parent = IssueKey("Issue ID"),
    description = Description("Description"),
    mentions = Mentions(setOf(User("Mention Name", "Mention ID"))),
    attachments = emptySet(),
    summary = Summary("Valid Summary")
  )

  private fun testNewIssueReport(block: suspend FlowAssert<UiModel>.() -> Unit) = testPresenter {
    presenter.sendEvent(UpdateInput { createSubTaskModel.input })
    expectItem()
    block()
  }
}
