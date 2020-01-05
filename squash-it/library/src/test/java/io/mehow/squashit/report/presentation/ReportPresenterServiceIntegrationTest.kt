package io.mehow.squashit.report.presentation

import io.kotlintest.shouldBe
import io.mehow.squashit.report.Description
import io.mehow.squashit.report.Epic
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.ReportType.UpdateIssue
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.User
import io.mehow.squashit.report.api.IssueTypeResponse
import io.mehow.squashit.report.api.ProjectResponse
import io.mehow.squashit.report.api.RoleFactory.Record
import io.mehow.squashit.report.presentation.Event.SubmitReport
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.extensions.withProjectInfo
import io.mehow.squashit.test
import org.junit.Test

internal class ReportPresenterServiceIntegrationTest : BaseReportPresenterTest() {
  @Test fun `blacklisted users are unavailable`() {
    val factory = presenterFactory.copy(
        config = presenterFactory.config.copy(userFilter = {
          it.accountId !in listOf("ID 1", "ID 4", "ID 5")
        })
    )
    factory.jiraApi.roleFactory.enqueue(
        Record("User 1", "ID 1"),
        Record("User 2", "ID 2"),
        Record("User 3", "ID 3"),
        Record("User 4", "ID 4")
    )
    presenterFactory = factory

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe syncedModel.withProjectInfo {
        copy(users = setOf(User("User 2", "ID 2"), User("User 3", "ID 3")))
      }
    }
  }

  @Test fun `whitelisted users are available`() {
    val factory = presenterFactory.copy(
        config = presenterFactory.config.copy(userFilter = {
          it.accountId in listOf("ID 1", "ID 4", "ID 5")
        })
    )
    factory.jiraApi.roleFactory.enqueue(
        Record("User 1", "ID 1"),
        Record("User 2", "ID 2"),
        Record("User 3", "ID 3"),
        Record("User 4", "ID 4")
    )
    presenterFactory = factory

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe syncedModel.withProjectInfo {
        copy(users = setOf(User("User 1", "ID 1"), User("User 4", "ID 4")))
      }
    }
  }

  @Test fun `blacklisted issue types are unavailable`() {
    val factory = presenterFactory.copy(
        config = presenterFactory.config.copy(issueTypeFilter = {
          it.id !in listOf("ID 2", "ID 5")
        })
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

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe syncedModel.withProjectInfo {
        copy(issueTypes = setOf(IssueType("ID 1", "Name 1"), IssueType("ID 4", "Name 4")))
      }
    }
  }

  @Test fun `whitelisted issue types are available`() {
    val factory = presenterFactory.copy(
        config = presenterFactory.config.copy(issueTypeFilter = {
          it.id in listOf("ID 2", "ID 5")
        })
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

    testPresenter(skipInitialization = false) {
      expectItem()
      expectItem() shouldBe syncedModel.withProjectInfo {
        copy(issueTypes = setOf(IssueType("ID 2", "Name 2")))
      }
    }
  }

  @Test fun `description is properly formatted for new issue`() = recordWithNewIssue {
    sendEvent(SubmitReport(newIssueInput))

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
        |Local date: 1970-01-01T00:00:00.000Z
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
    sendEvent(SubmitReport(addCommentInput))

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
        |Local date: 1970-01-01T00:00:00.000Z
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

  private val newIssueInput = syncedModel.input
      .withReporter(User("Reporter Name", "Reporter ID"))
      .withNewIssueType(IssueType("Issue ID", "Issue Name"))
      .withNewIssueSummary(Summary("Valid Summary"))
      .withNewIssueEpic(Epic("Epic ID", "Epic Name"))
      .withDescription(Description("Description"))
      .withMentions(User("Mention Name", "Mention ID"))

  private fun recordWithNewIssue(block: suspend ReportPresenter.() -> Unit) = testPresenter {
    presenter.sendEvent(UpdateInput { newIssueInput })
    presenter.block()
    cancelAndIgnoreRemainingEvents()
  }

  private val addCommentInput = syncedModel.input
      .withReportType(UpdateIssue)
      .withReporter(User("Reporter Name", "Reporter ID"))
      .withIssueKey(IssueKey("Issue ID"))
      .withDescription(Description("Description"))
      .withMentions(User("Mention Name", "Mention ID"))

  private fun recordWithAddComment(block: suspend ReportPresenter.() -> Unit) = testPresenter {
    presenter.sendEvent(UpdateInput { addCommentInput })
    presenter.block()
    cancelAndIgnoreRemainingEvents()
  }
}
