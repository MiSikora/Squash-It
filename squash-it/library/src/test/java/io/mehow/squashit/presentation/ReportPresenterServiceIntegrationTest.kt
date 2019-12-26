package io.mehow.squashit.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.Description
import io.mehow.squashit.Epic
import io.mehow.squashit.IssueKey
import io.mehow.squashit.IssueType
import io.mehow.squashit.ReportType.UpdateIssue
import io.mehow.squashit.Summary
import io.mehow.squashit.User
import io.mehow.squashit.api.IssueTypeResponse
import io.mehow.squashit.api.ProjectResponse
import io.mehow.squashit.api.RoleFactory.Record
import io.mehow.squashit.presentation.Event.MentionUser
import io.mehow.squashit.presentation.Event.SetDescription
import io.mehow.squashit.presentation.Event.SetEpic
import io.mehow.squashit.presentation.Event.SetIssueKey
import io.mehow.squashit.presentation.Event.SetIssueType
import io.mehow.squashit.presentation.Event.SetReportType
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.Event.SetSummary
import io.mehow.squashit.presentation.Event.SubmitReport
import io.mehow.squashit.presentation.extensions.withProjectInfo
import io.mehow.squashit.test
import org.junit.Test

internal class ReportPresenterServiceIntegrationTest : BaseReportPresenterTest() {
  @Test fun `unwanted users are filtered from project`() {
    val factory = presenterFactory.copy(
        config = presenterFactory.config.copy(filteredUsers = listOf("ID 1", "ID 4", "ID 5"))
    )
    factory.jiraApi.roleFactory.enqueue(
        Record("User 1", "ID 1"),
        Record("User 2", "ID 2"),
        Record("User 3", "ID 3"),
        Record("User 4", "ID 4")
    )
    presenterFactory = factory

    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe syncedModel.withProjectInfo {
        copy(users = setOf(User("User 2", "ID 2"), User("User 3", "ID 3")))
      }
    }
  }

  @Test fun `unwanted issue types are filtered from project`() {
    val factory = presenterFactory.copy(
        config = presenterFactory.config.copy(filteredIssueTypes = listOf("ID 2", "ID 5"))
    )
    factory.jiraApi.projectFactory.enqueue(
        ProjectResponse(
            listOf(
                IssueTypeResponse("ID 1", "Name 1", false),
                IssueTypeResponse("ID 2", "Name 2", false),
                IssueTypeResponse("ID 3", "Name 3", true),
                IssueTypeResponse("ID 4", "Name 4", false)
            ),
            mapOf("Role Name" to "Role ID")
        )
    )
    presenterFactory = factory

    testPresenter(skipSyncEvent = false) {
      expectItem() shouldBe syncedModel.withProjectInfo {
        copy(issueTypes = setOf(IssueType("ID 1", "Name 1"), IssueType("ID 4", "Name 4")))
      }
    }
  }

  @Test fun `description is properly formatted for new issue`() = recordWithNewIssue {
    sendEvent(SubmitReport)

    presenterFactory.jiraApi.newIssueRecords.test {
      expectItem().request.fields.description shouldBe """
        |{panel:title=Reporter notes}
        |Description
        |{panel}
        |
        |{panel:title=Application info}
        |Version name: version name
        |Version code: version code
        |Package name: package name
        |{panel}
        |
        |{panel:title=Device info}
        |Manufacturer: manufacturer
        |Model: model
        |Resolution: resolution
        |Density: density
        |Locales: [en_US]
        |Time zone: Greenwich Mean Time, GMT
        |{panel}
        |
        |{panel:title=OS info}
        |Release: release
        |SDK: 100
        |{panel}
        |
        |{panel:title=Mentions}
        |[Mention Name|~accountid:Mention ID]
        |{panel}
      """.trimMargin()

      expectComplete()
    }
  }

  @Test fun `body is properly formatted for comment`() = recordWithAddComment {
    sendEvent(SubmitReport)

    presenterFactory.jiraApi.addCommentRecords.test {
      expectItem().request.body shouldBe """
        |{panel:title=Reported by}
        |[Reporter Name|~accountid:Reporter ID]
        |{panel}
        |
        |{panel:title=Reporter notes}
        |Description
        |{panel}
        |
        |{panel:title=Application info}
        |Version name: version name
        |Version code: version code
        |Package name: package name
        |{panel}
        |
        |{panel:title=Device info}
        |Manufacturer: manufacturer
        |Model: model
        |Resolution: resolution
        |Density: density
        |Locales: [en_US]
        |Time zone: Greenwich Mean Time, GMT
        |{panel}
        |
        |{panel:title=OS info}
        |Release: release
        |SDK: 100
        |{panel}
        |
        |{panel:title=Mentions}
        |[Mention Name|~accountid:Mention ID]
        |{panel}
      """.trimMargin()

      expectComplete()
    }
  }

  private fun recordWithNewIssue(block: suspend ReportPresenter.() -> Unit) = testPresenter {
    sendEvent(SetReporter(User("Reporter Name", "Reporter ID")))
    sendEvent(SetIssueType(IssueType("Issue ID", "Issue Name")))
    sendEvent(SetSummary(Summary("Valid Summary")))
    sendEvent(SetEpic(Epic("Epic ID", "Epic Name")))
    sendEvent(SetDescription(Description("Description")))
    sendEvent(MentionUser(User("Mention Name", "Mention ID")))
    presenter.block()
    cancelAndIgnoreRemainingEvents()
  }

  private fun recordWithAddComment(block: suspend ReportPresenter.() -> Unit) = testPresenter {
    sendEvent(SetReportType(UpdateIssue))
    sendEvent(SetReporter(User("Reporter Name", "Reporter ID")))
    sendEvent(SetIssueKey(IssueKey("Issue ID")))
    sendEvent(SetDescription(Description("Description")))
    sendEvent(MentionUser(User("Mention Name", "Mention ID")))
    presenter.block()
    cancelAndIgnoreRemainingEvents()
  }
}
