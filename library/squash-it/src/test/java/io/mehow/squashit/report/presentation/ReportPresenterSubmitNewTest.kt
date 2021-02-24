package io.mehow.squashit.report.presentation

import app.cash.turbine.FlowTurbine
import io.kotest.matchers.shouldBe
import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.Description
import io.mehow.squashit.report.Epic
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.Mentions
import io.mehow.squashit.report.Report
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.User
import io.mehow.squashit.report.presentation.Event.Reattach
import io.mehow.squashit.report.presentation.Event.RetrySubmission
import io.mehow.squashit.report.presentation.Event.SubmitReport
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.extensions.withDescription
import io.mehow.squashit.report.presentation.extensions.withEpic
import io.mehow.squashit.report.presentation.extensions.withIssueType
import io.mehow.squashit.report.presentation.extensions.withLogs
import io.mehow.squashit.report.presentation.extensions.withMentions
import io.mehow.squashit.report.presentation.extensions.withReporter
import io.mehow.squashit.report.presentation.extensions.withSubmitState
import io.mehow.squashit.report.presentation.extensions.withSummary
import org.junit.Test

internal class ReportPresenterSubmitNewTest : BaseReportPresenterTest() {
  @Test fun `new issue can be created from UI model`() = testNewIssueReport {
    presenter.sendEvent(SubmitReport(newIssueModel.input))
    expectItem() shouldBe newIssueModel.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe newIssueModel.withSubmitState(
        SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `issue creation failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    presenter.sendEvent(SubmitReport(newIssueModel.input))
    expectItem()
    expectItem() shouldBe newIssueModel.withSubmitState(SubmitState.Failed(newIssueReport))
  }

  @Test fun `issue creation can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    presenter.sendEvent(SubmitReport(newIssueModel.input))
    expectItem()
    expectItem()

    presenterFactory.jiraApi.newIssueFactory.disableErrors()

    presenter.sendEvent(RetrySubmission(newIssueReport))
    expectItem() shouldBe newIssueModel.withSubmitState(SubmitState.Resubmitting)
    expectItem() shouldBe newIssueModel.withSubmitState(
        SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `add attachments failure does not matter without any attachments`() =
    testNewIssueReport {
      presenterFactory.jiraApi.attachmentsFactory.enableErrors()

      presenter.sendEvent(SubmitReport(newIssueModel.input))
      expectItem()
      expectItem() shouldBe newIssueModel.withSubmitState(
          SubmitState.Submitted(IssueKey("Issue ID"))
      )
    }

  @Test fun `add attachments failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    val model = newIssueModel.withLogs(AttachState.Attach(logsFile))
    val attachments = setOf(model.input.logsState.body!!)
    presenter.sendEvent(UpdateInput.logs(AttachState.Attach(logsFile)))
    expectItem()

    presenter.sendEvent(SubmitReport(newIssueModel.input.withLogs(AttachState.Attach(logsFile))))
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

    presenter.sendEvent(SubmitReport(newIssueModel.input.withLogs(AttachState.Attach(logsFile))))
    expectItem()
    expectItem()

    val model = newIssueModel.withLogs(AttachState.Attach(logsFile))
    val attachments = setOf(model.input.logsState.body!!)

    presenterFactory.jiraApi.attachmentsFactory.disableErrors()
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    presenter.sendEvent(Reattach(IssueKey("Issue ID"), attachments))
    expectItem() shouldBe model.withSubmitState(SubmitState.Reattaching)
    expectItem() shouldBe model.withSubmitState(
        SubmitState.AddedAttachments(IssueKey("Issue ID"))
    )
  }

  private val newIssueModel = syncedModel
      .withReporter(User("Reporter Name", "Reporter ID"))
      .withIssueType(IssueType("Issue ID", "Issue Name"))
      .withSummary(Summary("Valid Summary"))
      .withEpic(Epic("Epic ID", "Epic Name"))
      .withDescription(Description("Description"))
      .withMentions(User("Mention Name", "Mention ID"))

  private val newIssueReport = Report.NewIssue(
      description = Description("Description"),
      mentions = Mentions(setOf(User("Mention Name", "Mention ID"))),
      attachments = emptySet(),
      reporter = User("Reporter Name", "Reporter ID"),
      issueType = IssueType("Issue ID", "Issue Name"),
      summary = Summary("Valid Summary"),
      epic = Epic("Epic ID", "Epic Name")
  )

  private fun testNewIssueReport(block: suspend FlowTurbine<UiModel>.() -> Unit) = testPresenter {
    presenter.sendEvent(UpdateInput { newIssueModel.input })
    expectItem()
    block()
  }
}
