package io.mehow.squashit.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.AttachState
import io.mehow.squashit.Description
import io.mehow.squashit.Epic
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.Mentions
import io.mehow.squashit.NewIssue
import io.mehow.squashit.Report
import io.mehow.squashit.SubmitState
import io.mehow.squashit.Summary
import io.mehow.squashit.User
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.presentation.Event.MentionUser
import io.mehow.squashit.presentation.Event.RetryAttachmentsForNew
import io.mehow.squashit.presentation.Event.RetrySubmission
import io.mehow.squashit.presentation.Event.SetIssueDescription
import io.mehow.squashit.presentation.Event.SetLogsState
import io.mehow.squashit.presentation.Event.SetNewIssueEpic
import io.mehow.squashit.presentation.Event.SetNewIssueSummary
import io.mehow.squashit.presentation.Event.SetNewIssueType
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.Event.SubmitReport
import io.mehow.squashit.presentation.extensions.PresenterAssert
import org.junit.Test

class ReportPresenterSubmitNewTest : BaseReportPresenterTest() {
  @Test fun `new issue can be created from UI model`() = testNewIssueReport {
    sendEvent(SubmitReport)
    expectItem() shouldBe newIssueModel.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe newIssueModel.copy(
        submitState = SubmitState.CreatedNew(IssueKey("Issue ID"))
    )
  }

  @Test fun `issue creation failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    sendEvent(SubmitReport)
    expectItem()
    expectItem() shouldBe newIssueModel.copy(submitState = SubmitState.Failed(newIssueReport))
  }

  @Test fun `issue creation can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    presenterFactory.jiraApi.newIssueFactory.disableErrors()

    sendEvent(RetrySubmission(newIssueReport))
    expectItem() shouldBe newIssueModel.copy(submitState = SubmitState.RetryingSubmission)
    expectItem() shouldBe newIssueModel.copy(
        submitState = SubmitState.CreatedNew(IssueKey("Issue ID"))
    )
  }

  @Test fun `add attachments failure does not matter without any attachments`() =
    testNewIssueReport {
      presenterFactory.jiraApi.attachmentsFactory.enableErrors()

      sendEvent(SubmitReport)
      expectItem()
      expectItem() shouldBe newIssueModel.copy(
          submitState = SubmitState.CreatedNew(IssueKey("Issue ID"))
      )
    }

  @Test fun `add attachments failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    val model = newIssueModel.copy(logsState = AttachState.Attach(logsFile))
    val attachments = setOf(AttachmentBody.fromFile(logsFile))
    sendEvent(SetLogsState(AttachState.Attach(logsFile)))
    expectItem()

    sendEvent(SubmitReport)
    expectItem()
    expectItem() shouldBe model.copy(
        submitState = SubmitState.FailedToAttachForNew(IssueKey("Issue ID"), attachments)
    )
  }

  @Test fun `adding attachments can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    sendEvent(SetLogsState(AttachState.Attach(logsFile)))
    expectItem()

    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    val model = newIssueModel.copy(logsState = AttachState.Attach(logsFile))
    val attachments = setOf(AttachmentBody.fromFile(logsFile))

    presenterFactory.jiraApi.attachmentsFactory.disableErrors()
    presenterFactory.jiraApi.newIssueFactory.enableErrors()

    sendEvent(RetryAttachmentsForNew(IssueKey("Issue ID"), attachments))
    expectItem() shouldBe model.copy(submitState = SubmitState.RetryingAttachmentsForNew)
    expectItem() shouldBe model.copy(
        submitState = SubmitState.AddedAttachments(IssueKey("Issue ID"))
    )
  }

  private val newIssueModel = syncedModel.copy(
      reporter = User("Reporter Name", "Reporter ID"),
      newIssue = NewIssue(
          type = IssueType("Issue ID", "Issue Name"),
          summary = Summary("Valid Summary"),
          epic = Epic("Epic ID", "Epic Name")
      ),
      issueDescription = Description("Description"),
      mentions = Mentions(setOf(User("Mention Name", "Mention ID")))
  )

  private val newIssueReport = Report.NewIssue(
      description = Description("Description"),
      mentions = Mentions(setOf(User("Mention Name", "Mention ID"))),
      attachments = emptySet(),
      reporter = User("Reporter Name", "Reporter ID"),
      issueType = IssueType("Issue ID", "Issue Name"),
      summary = Summary("Valid Summary"),
      epic = Epic("Epic ID", "Epic Name")
  )

  private fun testNewIssueReport(block: suspend PresenterAssert.() -> Unit) = testPresenter {
    sendEvent(SetReporter(User("Reporter Name", "Reporter ID")))
    expectItem()
    sendEvent(SetNewIssueType(IssueType("Issue ID", "Issue Name")))
    expectItem()
    sendEvent(SetNewIssueSummary(Summary("Valid Summary")))
    expectItem()
    sendEvent(SetNewIssueEpic(Epic("Epic ID", "Epic Name")))
    expectItem()
    sendEvent(SetIssueDescription(Description("Description")))
    expectItem()
    sendEvent(MentionUser(User("Mention Name", "Mention ID")))
    expectItem()
    block()
  }
}
