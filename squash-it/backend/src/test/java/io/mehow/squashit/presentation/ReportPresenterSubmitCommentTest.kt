package io.mehow.squashit.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.AttachState
import io.mehow.squashit.Description
import io.mehow.squashit.IssueKey
import io.mehow.squashit.Mentions
import io.mehow.squashit.Report
import io.mehow.squashit.ReportType
import io.mehow.squashit.Reporter
import io.mehow.squashit.SubmitState
import io.mehow.squashit.User
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.presentation.Event.MentionUser
import io.mehow.squashit.presentation.Event.Reattach
import io.mehow.squashit.presentation.Event.RetrySubmission
import io.mehow.squashit.presentation.Event.SetDescription
import io.mehow.squashit.presentation.Event.SetIssueKey
import io.mehow.squashit.presentation.Event.SetLogsState
import io.mehow.squashit.presentation.Event.SetReportType
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.Event.SubmitReport
import io.mehow.squashit.presentation.extensions.PresenterAssert
import org.junit.Test

class ReportPresenterSubmitCommentTest : BaseReportPresenterTest() {
  @Test fun `comment can be created from UI model`() = testNewIssueReport {
    sendEvent(SubmitReport)
    expectItem() shouldBe addCommentModel.copy(submitState = SubmitState.Submitting)
    expectItem() shouldBe addCommentModel.copy(
        submitState = SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `comment creation failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.commentFactory.enableErrors()

    sendEvent(SubmitReport)
    expectItem()
    expectItem() shouldBe addCommentModel.copy(submitState = SubmitState.Failed(addCommentReport))
  }

  @Test fun `comment creation can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.commentFactory.enableErrors()

    sendEvent(SubmitReport)
    expectItem()
    expectItem()

    presenterFactory.jiraApi.commentFactory.disableErrors()

    sendEvent(RetrySubmission(addCommentReport))
    expectItem() shouldBe addCommentModel.copy(submitState = SubmitState.Resubmitting)
    expectItem() shouldBe addCommentModel.copy(
        submitState = SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `add attachments failure does not matter without any attachments`() =
    testNewIssueReport {
      presenterFactory.jiraApi.attachmentsFactory.enableErrors()

      sendEvent(SubmitReport)
      expectItem()
      expectItem() shouldBe addCommentModel.copy(
          submitState = SubmitState.Submitted(IssueKey("Issue ID"))
      )
    }

  @Test fun `add attachments failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    val model = addCommentModel.copy(logsState = AttachState.Attach(logsFile))
    val attachments = setOf(AttachmentBody.fromFile(logsFile))
    sendEvent(SetLogsState(AttachState.Attach(logsFile)))
    expectItem()

    sendEvent(SubmitReport)
    expectItem()
    expectItem() shouldBe model.copy(
        submitState = SubmitState.FailedToAttach(IssueKey("Issue ID"), attachments)
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

    val model = addCommentModel.copy(logsState = AttachState.Attach(logsFile))
    val attachments = setOf(AttachmentBody.fromFile(logsFile))

    presenterFactory.jiraApi.attachmentsFactory.disableErrors()
    presenterFactory.jiraApi.commentFactory.enableErrors()

    sendEvent(Reattach(IssueKey("Issue ID"), attachments))
    expectItem() shouldBe model.copy(submitState = SubmitState.Reattaching)
    expectItem() shouldBe model.copy(
        submitState = SubmitState.AddedAttachments(IssueKey("Issue ID"))
    )
  }

  private val addCommentModel = syncedModel.copy(
      reportType = ReportType.UpdateIssue,
      reporter = User("Reporter Name", "Reporter ID"),
      updateIssueKey = IssueKey("Issue ID"),
      issueDescription = Description("Description"),
      mentions = Mentions(setOf(User("Mention Name", "Mention ID")))
  )

  private val addCommentReport = Report.AddComment(
      reporter = Reporter(User("Reporter Name", "Reporter ID")),
      issueKey = IssueKey("Issue ID"),
      description = Description("Description"),
      mentions = Mentions(setOf(User("Mention Name", "Mention ID"))),
      attachments = emptySet()
  )

  private fun testNewIssueReport(block: suspend PresenterAssert.() -> Unit) = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem()
    sendEvent(SetReporter(User("Reporter Name", "Reporter ID")))
    expectItem()
    sendEvent(SetIssueKey(IssueKey("Issue ID")))
    expectItem()
    sendEvent(SetDescription(Description("Description")))
    expectItem()
    sendEvent(MentionUser(User("Mention Name", "Mention ID")))
    expectItem()
    block()
  }
}
