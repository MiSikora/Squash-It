package io.mehow.squashit.report.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.Attachment
import io.mehow.squashit.report.AttachmentType.Image
import io.mehow.squashit.report.AttachmentType.Video
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
import io.mehow.squashit.report.presentation.extensions.withIssueKey
import io.mehow.squashit.report.presentation.extensions.withLogs
import io.mehow.squashit.report.presentation.extensions.withMentions
import io.mehow.squashit.report.presentation.extensions.withNewIssueEpic
import io.mehow.squashit.report.presentation.extensions.withNewIssueSummary
import io.mehow.squashit.report.presentation.extensions.withNewIssueType
import io.mehow.squashit.report.presentation.extensions.withReportType
import io.mehow.squashit.report.presentation.extensions.withReporter
import io.mehow.squashit.report.presentation.extensions.withScreenshot
import org.junit.Test

internal class ReportPresenterInputTest : BaseReportPresenterTest() {
  @Test fun `reporter can be changed`() = testPresenter {
    sendEvent(UpdateInput.reporter(User("Name", "ID")))
    expectItem() shouldBe syncedModel.withReporter(
        User(
            "Name",
            "ID"
        )
    )
  }

  @Test fun `report type can be changed`() = testPresenter {
    sendEvent(UpdateInput.reportType(ReportType.UpdateIssue))
    expectItem() shouldBe syncedModel.withReportType(ReportType.UpdateIssue)

    sendEvent(UpdateInput.reportType(ReportType.CreateNewIssue))
    expectItem() shouldBe syncedModel
  }

  @Test fun `issue type can be changed`() = testPresenter {
    sendEvent(UpdateInput.issueType(
        IssueType(
            "ID",
            "Name"
        )
    ))
    expectItem() shouldBe syncedModel.withNewIssueType(
        IssueType(
            "ID",
            "Name"
        )
    )
  }

  @Test fun `new issue summary can be changed`() = testPresenter {
    sendEvent(UpdateInput.summary(Summary("Summary")))
    expectItem() shouldBe syncedModel.withNewIssueSummary(
        Summary(
            "Summary"
        )
    )
  }

  @Test fun `new issue epic can be changed`() = testPresenter {
    sendEvent(UpdateInput.epic(Epic("ID", "Name")))
    expectItem() shouldBe syncedModel.withNewIssueEpic(
        Epic(
            "ID",
            "Name"
        )
    )
  }

  @Test fun `issue key can be changed`() = testPresenter {
    sendEvent(UpdateInput.issueKey(IssueKey("Key")))
    expectItem() shouldBe syncedModel.withIssueKey(
        IssueKey(
            "Key"
        )
    )
  }

  @Test fun `issue description can bed changed`() = testPresenter {
    sendEvent(UpdateInput.description(Description("Description")))
    expectItem() shouldBe syncedModel.withDescription(
        Description(
            "Description"
        )
    )
  }

  @Test fun `users can be mentioned`() = testPresenter {
    sendEvent(UpdateInput.mention(
        User(
            "Name 1",
            "ID 1"
        )
    ))
    expectItem() shouldBe syncedModel.withMentions(
        User(
            "Name 1",
            "ID 1"
        )
    )

    sendEvent(UpdateInput.mention(
        User(
            "Name 2",
            "ID 2"
        )
    ))
    expectItem() shouldBe syncedModel.withMentions(
        User(
            "Name 1",
            "ID 1"
        ), User("Name 2", "ID 2")
    )
  }

  @Test fun `users can be unmentioned`() = testPresenter {
    sendEvent(UpdateInput.mention(
        User(
            "Name 1",
            "ID 1"
        )
    ))
    expectItem()

    sendEvent(UpdateInput.mention(
        User(
            "Name 2",
            "ID 2"
        )
    ))
    expectItem()

    sendEvent(UpdateInput.unmention(
        User(
            "Name 3",
            "ID 3"
        )
    ))
    expectNoEvents()

    sendEvent(UpdateInput.unmention(
        User(
            "Name 1",
            "ID 1"
        )
    ))
    expectItem() shouldBe syncedModel.withMentions(
        User(
            "Name 2",
            "ID 2"
        )
    )
  }

  @Test fun `screenshot state can be changed`() = testPresenter {
    val screenshot = folder.newFile()
    sendEvent(UpdateInput.screenshot(AttachState.Attach(screenshot)))
    expectItem() shouldBe syncedModel.withScreenshot(AttachState.Attach(screenshot))

    sendEvent(UpdateInput.screenshot(AttachState.DoNotAttach(screenshot)))
    expectItem() shouldBe syncedModel.withScreenshot(AttachState.DoNotAttach(screenshot))
  }

  @Test fun `logs state can be changed`() = testPresenter {
    val logs = folder.newFile()
    sendEvent(UpdateInput.logs(AttachState.Attach(logs)))
    expectItem() shouldBe syncedModel.withLogs(AttachState.Attach(logs))

    sendEvent(UpdateInput.logs(AttachState.DoNotAttach(logs)))
    expectItem() shouldBe syncedModel.withLogs(AttachState.DoNotAttach(logs))
  }

  @Test fun `custom attachments can be added`() = testPresenter {
    val attachment1 = Attachment(
        Image,
        "Name 1",
        "Size 1"
    ) { null }
    sendEvent(UpdateInput.attach(attachment1))
    expectItem() shouldBe syncedModel.withAttachments(attachment1)

    val attachment2 = Attachment(
        Video,
        "Name 2",
        "Size 2"
    ) { null }
    sendEvent(UpdateInput.attach(attachment2))
    expectItem() shouldBe syncedModel.withAttachments(attachment1, attachment2)
  }

  @Test fun `custom attachments can be removed`() = testPresenter {
    val attachment1 = Attachment(
        Image,
        "Name 1",
        "Size 1"
    ) { null }
    val attachment2 = Attachment(
        Video,
        "Name 2",
        "Size 2"
    ) { null }
    val attachment3 = Attachment(
        Video,
        "Name 3",
        "Size 3"
    ) { null }
    sendEvent(UpdateInput.attach(attachment1))
    expectItem()

    sendEvent(UpdateInput.attach(attachment2))
    expectItem()

    sendEvent(UpdateInput.detach(attachment3))
    expectNoEvents()

    sendEvent(UpdateInput.detach(attachment1))
    expectItem() shouldBe syncedModel.withAttachments(attachment2)
  }
}
