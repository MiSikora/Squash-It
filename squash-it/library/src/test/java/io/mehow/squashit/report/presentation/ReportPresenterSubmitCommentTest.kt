package io.mehow.squashit.report.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.Description
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.Mentions
import io.mehow.squashit.report.Report
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.Reporter
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.User
import io.mehow.squashit.report.api.AttachmentBody
import io.mehow.squashit.report.presentation.Event.Reattach
import io.mehow.squashit.report.presentation.Event.RetrySubmission
import io.mehow.squashit.report.presentation.Event.SubmitReport
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.extensions.PresenterAssert
import io.mehow.squashit.report.presentation.extensions.withDescription
import io.mehow.squashit.report.presentation.extensions.withIssueKey
import io.mehow.squashit.report.presentation.extensions.withLogs
import io.mehow.squashit.report.presentation.extensions.withMentions
import io.mehow.squashit.report.presentation.extensions.withReportType
import io.mehow.squashit.report.presentation.extensions.withReporter
import io.mehow.squashit.report.presentation.extensions.withSubmitState
import org.junit.Test

internal class ReportPresenterSubmitCommentTest : BaseReportPresenterTest() {
  @Test fun `comment can be created from UI model`() = testNewIssueReport {
    sendEvent(SubmitReport(addCommentModel.input))
    expectItem() shouldBe addCommentModel.withSubmitState(SubmitState.Submitting)
    expectItem() shouldBe addCommentModel.withSubmitState(
        SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `comment creation failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.commentFactory.enableErrors()

    sendEvent(SubmitReport(addCommentModel.input))
    expectItem()
    expectItem() shouldBe addCommentModel.withSubmitState(SubmitState.Failed(addCommentReport))
  }

  @Test fun `comment creation can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.commentFactory.enableErrors()

    sendEvent(SubmitReport(addCommentModel.input))
    expectItem()
    expectItem()

    presenterFactory.jiraApi.commentFactory.disableErrors()

    sendEvent(RetrySubmission(addCommentReport))
    expectItem() shouldBe addCommentModel.withSubmitState(SubmitState.Resubmitting)
    expectItem() shouldBe addCommentModel.withSubmitState(
        SubmitState.Submitted(IssueKey("Issue ID"))
    )
  }

  @Test fun `add attachments failure does not matter without any attachments`() =
    testNewIssueReport {
      presenterFactory.jiraApi.attachmentsFactory.enableErrors()

      sendEvent(SubmitReport(addCommentModel.input))
      expectItem()
      expectItem() shouldBe addCommentModel.withSubmitState(
          SubmitState.Submitted(IssueKey("Issue ID"))
      )
    }

  @Test fun `add attachments failure is handled gracefully`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    val model = addCommentModel.withLogs(AttachState.Attach(logsFile))
    val attachments = setOf(AttachmentBody.fromFile(logsFile))
    sendEvent(UpdateInput.logs(AttachState.Attach(logsFile)))
    expectItem()

    sendEvent(SubmitReport(addCommentModel.input.withLogs(AttachState.Attach(logsFile))))
    expectItem()
    expectItem() shouldBe model.withSubmitState(
        SubmitState.FailedToAttach(IssueKey("Issue ID"), attachments)
    )
  }

  @Test fun `adding attachments can be retried`() = testNewIssueReport {
    presenterFactory.jiraApi.attachmentsFactory.enableErrors()

    val logsFile = folder.newFile()
    sendEvent(UpdateInput.logs(AttachState.Attach(logsFile)))
    expectItem()

    sendEvent(SubmitReport(addCommentModel.input.withLogs(AttachState.Attach(logsFile))))
    expectItem()
    expectItem()

    val model = addCommentModel.withLogs(AttachState.Attach(logsFile))
    val attachments = setOf(AttachmentBody.fromFile(logsFile))

    presenterFactory.jiraApi.attachmentsFactory.disableErrors()
    presenterFactory.jiraApi.commentFactory.enableErrors()

    sendEvent(Reattach(IssueKey("Issue ID"), attachments))
    expectItem() shouldBe model.withSubmitState(SubmitState.Reattaching)
    expectItem() shouldBe model.withSubmitState(
        SubmitState.AddedAttachments(
        IssueKey(
            "Issue ID"
        )
    ))
  }

  private val addCommentModel = syncedModel
      .withReportType(ReportType.UpdateIssue)
      .withReporter(
          User(
              "Reporter Name",
              "Reporter ID"
          )
      )
      .withIssueKey(IssueKey("Issue ID"))
      .withDescription(Description("Description"))
      .withMentions(User("Mention Name", "Mention ID"))

  private val addCommentReport = Report.AddComment(
      reporter = Reporter(
          User(
              "Reporter Name",
              "Reporter ID"
          )
      ),
      issueKey = IssueKey("Issue ID"),
      description = Description("Description"),
      mentions = Mentions(
          setOf(
              User(
                  "Mention Name",
                  "Mention ID"
              )
          )
      ),
      attachments = emptySet()
  )

  private fun testNewIssueReport(block: suspend PresenterAssert.() -> Unit) = testPresenter {
    sendEvent(UpdateInput { addCommentModel.input })
    expectItem()
    block()
  }
}
