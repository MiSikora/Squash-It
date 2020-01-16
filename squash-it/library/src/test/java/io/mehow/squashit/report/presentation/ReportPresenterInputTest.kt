package io.mehow.squashit.report.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.Attachment
import io.mehow.squashit.report.AttachmentId
import io.mehow.squashit.report.Description
import io.mehow.squashit.report.Epic
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.User
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.extensions.withAttachments
import io.mehow.squashit.report.presentation.extensions.withDescription
import io.mehow.squashit.report.presentation.extensions.withEpic
import io.mehow.squashit.report.presentation.extensions.withIssueKey
import io.mehow.squashit.report.presentation.extensions.withIssueType
import io.mehow.squashit.report.presentation.extensions.withLogs
import io.mehow.squashit.report.presentation.extensions.withMentions
import io.mehow.squashit.report.presentation.extensions.withReportType
import io.mehow.squashit.report.presentation.extensions.withReporter
import io.mehow.squashit.report.presentation.extensions.withScreenshot
import io.mehow.squashit.report.presentation.extensions.withSummary
import org.junit.Test

internal class ReportPresenterInputTest : BaseReportPresenterTest() {
  @Test fun `reporter can be changed`() = testPresenter {
    presenter.sendEvent(UpdateInput.reporter(User("Name", "ID")))
    expectItem() shouldBe syncedModel.withReporter(User("Name", "ID"))
  }

  @Test fun `report type can be changed`() = testPresenter {
    presenter.sendEvent(UpdateInput.reportType(ReportType.AddCommentToIssue))
    expectItem() shouldBe syncedModel.withReportType(ReportType.AddCommentToIssue)

    presenter.sendEvent(UpdateInput.reportType(ReportType.CreateNewIssue))
    expectItem() shouldBe syncedModel
  }

  @Test fun `issue type can be changed`() = testPresenter {
    presenter.sendEvent(UpdateInput.issueType(IssueType("ID", "Name")))
    expectItem() shouldBe syncedModel.withIssueType(IssueType("ID", "Name"))
  }

  @Test fun `new issue summary can be changed`() = testPresenter {
    presenter.sendEvent(UpdateInput.summary(Summary("Summary")))
    expectItem() shouldBe syncedModel.withSummary(Summary("Summary"))
  }

  @Test fun `new issue epic can be changed`() = testPresenter {
    presenter.sendEvent(UpdateInput.epic(Epic("ID", "Name")))
    expectItem() shouldBe syncedModel.withEpic(Epic("ID", "Name"))
  }

  @Test fun `issue key can be changed`() = testPresenter {
    presenter.sendEvent(UpdateInput.issueKey(IssueKey("Key")))
    expectItem() shouldBe syncedModel.withIssueKey(IssueKey("Key"))
  }

  @Test fun `issue description can bed changed`() = testPresenter {
    presenter.sendEvent(UpdateInput.description(Description("Description")))
    expectItem() shouldBe syncedModel.withDescription(Description("Description"))
  }

  @Test fun `users can be mentioned`() = testPresenter {
    presenter.sendEvent(UpdateInput.mention(User("Name 1", "ID 1")))
    expectItem() shouldBe syncedModel.withMentions(User("Name 1", "ID 1"))

    presenter.sendEvent(UpdateInput.mention(User("Name 2", "ID 2")))
    expectItem() shouldBe syncedModel.withMentions(User("Name 1", "ID 1"), User("Name 2", "ID 2"))
  }

  @Test fun `users can be unmentioned`() = testPresenter {
    presenter.sendEvent(UpdateInput.mention(User("Name 1", "ID 1")))
    expectItem()

    presenter.sendEvent(UpdateInput.mention(User("Name 2", "ID 2")))
    expectItem()

    presenter.sendEvent(UpdateInput.unmention(User("Name 3", "ID 3")))
    expectNoEvents()

    presenter.sendEvent(UpdateInput.unmention(User("Name 1", "ID 1")))
    expectItem() shouldBe syncedModel.withMentions(User("Name 2", "ID 2"))
  }

  @Test fun `screenshot state can be changed`() = testPresenter {
    val screenshot = folder.newFile()
    presenter.sendEvent(UpdateInput.screenshot(AttachState.Attach(screenshot)))
    expectItem() shouldBe syncedModel.withScreenshot(AttachState.Attach(screenshot))

    presenter.sendEvent(UpdateInput.screenshot(AttachState.DoNotAttach(screenshot)))
    expectItem() shouldBe syncedModel.withScreenshot(AttachState.DoNotAttach(screenshot))
  }

  @Test fun `logs state can be changed`() = testPresenter {
    val logs = folder.newFile()
    presenter.sendEvent(UpdateInput.logs(AttachState.Attach(logs)))
    expectItem() shouldBe syncedModel.withLogs(AttachState.Attach(logs))

    presenter.sendEvent(UpdateInput.logs(AttachState.DoNotAttach(logs)))
    expectItem() shouldBe syncedModel.withLogs(AttachState.DoNotAttach(logs))
  }

  @Test fun `custom attachments can be added`() = testPresenter {
    val attachment1 = Attachment(AttachmentId("ID 1"), "Name 1") { null }
    presenter.sendEvent(UpdateInput.attach(attachment1))
    expectItem() shouldBe syncedModel.withAttachments(attachment1)

    val attachment2 = Attachment(AttachmentId("ID 1"), "Name 2") { null }
    presenter.sendEvent(UpdateInput.attach(attachment2))
    expectItem() shouldBe syncedModel.withAttachments(attachment1, attachment2)
  }

  @Test fun `custom attachments can be removed`() = testPresenter {
    val attachment1 = Attachment(AttachmentId("ID 1"), "Name 1") { null }
    val attachment2 = Attachment(AttachmentId("ID 2"), "Name 2") { null }
    val attachment3 = Attachment(AttachmentId("ID 3"), "Name 3") { null }
    presenter.sendEvent(UpdateInput.attach(attachment1))
    expectItem()

    presenter.sendEvent(UpdateInput.attach(attachment2))
    expectItem()

    presenter.sendEvent(UpdateInput.detach(attachment3.id))
    expectNoEvents()

    presenter.sendEvent(UpdateInput.detach(attachment1.id))
    expectItem() shouldBe syncedModel.withAttachments(attachment2)
  }
}
