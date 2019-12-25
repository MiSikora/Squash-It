package io.mehow.squashit.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.AttachState
import io.mehow.squashit.Attachment
import io.mehow.squashit.AttachmentType
import io.mehow.squashit.Description
import io.mehow.squashit.Epic
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.Mentions
import io.mehow.squashit.NewIssue
import io.mehow.squashit.ReportType
import io.mehow.squashit.Summary
import io.mehow.squashit.User
import io.mehow.squashit.presentation.Event.AddAttachment
import io.mehow.squashit.presentation.Event.MentionUser
import io.mehow.squashit.presentation.Event.RemoveAttachment
import io.mehow.squashit.presentation.Event.SetDescription
import io.mehow.squashit.presentation.Event.SetEpic
import io.mehow.squashit.presentation.Event.SetIssueKey
import io.mehow.squashit.presentation.Event.SetIssueType
import io.mehow.squashit.presentation.Event.SetLogsState
import io.mehow.squashit.presentation.Event.SetReportType
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.Event.SetScreenshotState
import io.mehow.squashit.presentation.Event.SetSummary
import io.mehow.squashit.presentation.Event.UnmentionUser
import org.junit.Test

class ReportPresenterInputTest : BaseReportPresenterTest() {
  @Test fun `reporter can be changed`() = testPresenter {
    sendEvent(SetReporter(User("Name", "ID")))
    expectItem() shouldBe syncedModel.copy(reporter = User("Name", "ID"))
  }

  @Test fun `report type can be changed`() = testPresenter {
    sendEvent(SetReportType(ReportType.UpdateIssue))
    expectItem() shouldBe syncedModel.copy(reportType = ReportType.UpdateIssue)

    sendEvent(SetReportType(ReportType.CreateNewIssue))
    expectItem() shouldBe syncedModel
  }

  @Test fun `issue type can be changed`() = testPresenter {
    sendEvent(SetIssueType(IssueType("ID", "Name")))
    expectItem() shouldBe syncedModel.copy(newIssue = NewIssue(IssueType("ID", "Name"), null, null))
  }

  @Test fun `new issue summary can be changed`() = testPresenter {
    sendEvent(SetSummary(Summary("Summary")))
    expectItem() shouldBe syncedModel.copy(newIssue = NewIssue(null, Summary("Summary"), null))
  }

  @Test fun `new issue epic can be changed`() = testPresenter {
    sendEvent(SetEpic(Epic("ID", "Name")))
    expectItem() shouldBe syncedModel.copy(newIssue = NewIssue(null, null, Epic("ID", "Name")))
  }

  @Test fun `issue key can be changed`() = testPresenter {
    sendEvent(SetIssueKey(IssueKey("Key")))
    expectItem() shouldBe syncedModel.copy(issueKey = IssueKey("Key"))
  }

  @Test fun `issue description can bed changed`() = testPresenter {
    sendEvent(SetDescription(Description("Description")))
    expectItem() shouldBe syncedModel.copy(issueDescription = Description("Description"))
  }

  @Test fun `users can be mentioned`() = testPresenter {
    sendEvent(MentionUser(User("Name 1", "ID 1")))
    expectItem() shouldBe syncedModel.copy(mentions = Mentions(setOf(User("Name 1", "ID 1"))))

    sendEvent(MentionUser(User("Name 2", "ID 2")))
    expectItem() shouldBe syncedModel.copy(
        mentions = Mentions(setOf(User("Name 1", "ID 1"), User("Name 2", "ID 2")))
    )
  }

  @Test fun `users can be unmentioned`() = testPresenter {
    sendEvent(MentionUser(User("Name 1", "ID 1")))
    expectItem()

    sendEvent(MentionUser(User("Name 2", "ID 2")))
    expectItem()

    sendEvent(UnmentionUser(User("Name 3", "ID 3")))
    expectNoEvents()

    sendEvent(UnmentionUser(User("Name 1", "ID 1")))
    expectItem() shouldBe syncedModel.copy(mentions = Mentions(setOf(User("Name 2", "ID 2"))))
  }

  @Test fun `screenshot state can be changed`() = testPresenter {
    val screenshot = folder.newFile()
    sendEvent(SetScreenshotState(AttachState.Attach(screenshot)))
    expectItem() shouldBe syncedModel.copy(screenshotState = AttachState.Attach(screenshot))

    sendEvent(SetScreenshotState(AttachState.DoNotAttach(screenshot)))
    expectItem() shouldBe syncedModel.copy(screenshotState = AttachState.DoNotAttach(screenshot))
  }

  @Test fun `logs state can be changed`() = testPresenter {
    val logs = folder.newFile()
    sendEvent(SetLogsState(AttachState.Attach(logs)))
    expectItem() shouldBe syncedModel.copy(logsState = AttachState.Attach(logs))

    sendEvent(SetLogsState(AttachState.DoNotAttach(logs)))
    expectItem() shouldBe syncedModel.copy(logsState = AttachState.DoNotAttach(logs))
  }

  @Test fun `custom attachments can be added`() = testPresenter {
    val attachment1 = Attachment(AttachmentType.Image, "Name 1", "Size 1") { null }
    sendEvent(AddAttachment(attachment1))
    expectItem() shouldBe syncedModel.copy(attachments = setOf(attachment1))

    val attachment2 = Attachment(AttachmentType.Video, "Name 2", "Size 2") { null }
    sendEvent(AddAttachment(attachment2))
    expectItem() shouldBe syncedModel.copy(attachments = setOf(attachment1, attachment2))
  }

  @Test fun `custom attachments can be removed`() = testPresenter {
    val attachment1 = Attachment(AttachmentType.Image, "Name 1", "Size 1") { null }
    val attachment2 = Attachment(AttachmentType.Video, "Name 2", "Size 2") { null }
    val attachment3 = Attachment(AttachmentType.Video, "Name 3", "Size 3") { null }
    sendEvent(AddAttachment(attachment1))
    expectItem()

    sendEvent(AddAttachment(attachment2))
    expectItem()

    sendEvent(RemoveAttachment(attachment3))
    expectNoEvents()

    sendEvent(RemoveAttachment(attachment1))
    expectItem() shouldBe syncedModel.copy(attachments = setOf(attachment2))
  }
}
