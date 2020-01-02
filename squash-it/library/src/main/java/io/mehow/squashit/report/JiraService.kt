package io.mehow.squashit.report

import io.mehow.squashit.report.Report.AddComment
import io.mehow.squashit.report.Report.NewIssue
import io.mehow.squashit.report.api.AddCommentRequest
import io.mehow.squashit.report.api.AttachmentBody
import io.mehow.squashit.report.api.EpicJql
import io.mehow.squashit.report.api.IssueTypeRequest
import io.mehow.squashit.report.api.JiraApi
import io.mehow.squashit.report.api.NewIssueFieldsRequest
import io.mehow.squashit.report.api.NewIssueRequest
import io.mehow.squashit.report.api.ProjectRequest
import io.mehow.squashit.report.api.ReporterRequest
import io.mehow.squashit.report.api.Response
import io.mehow.squashit.report.api.Response.Failure
import io.mehow.squashit.report.api.Response.Success
import io.mehow.squashit.report.api.RoleResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class JiraService(
  private val config: ReportConfig.Valid,
  private val projectInfoStore: ProjectInfoStore,
  private val jiraApi: JiraApi
) {
  suspend fun getProjectInfo(): ProjectInfo? {
    val cachedInfo = projectInfoStore.read()
    if (cachedInfo != null) return cachedInfo
    val (issueTypes, roleIds) = getIssuesTypesAndRoleIds() ?: return null
    if (issueTypes.isEmpty()) return null

    val users = getUsers(roleIds).toSet()
    if (users.isEmpty()) return null

    val epics = getEpics().toSet()

    return ProjectInfo(epics, users, issueTypes.toSet()).also { projectInfoStore.save(it) }
  }

  suspend fun createNewIssue(report: NewIssue): CreateReportAttempt {
    val newIssueRequest = createNewIssueRequest(report)
    when (val createResponse = jiraApi.createNewIssue(newIssueRequest)) {
      is Success -> {
        val key = IssueKey(createResponse.value.key)
        if (report.attachments.isEmpty()) return CreateReportAttempt.Success(key)

        return when (addAttachments(key, report.attachments)) {
          is Success -> CreateReportAttempt.Success(key)
          is Failure -> CreateReportAttempt.NoAttachments(key)
        }
      }
      is Failure -> return CreateReportAttempt.Failure
    }
  }

  suspend fun addComment(report: AddComment): CreateReportAttempt {
    val key = report.issueKey
    val addCommentRequest = createAddCommentRequest(report)
    when (jiraApi.addComment(key, addCommentRequest)) {
      is Success -> {
        if (report.attachments.isEmpty()) return CreateReportAttempt.Success(key)

        return when (addAttachments(key, report.attachments)) {
          is Success -> CreateReportAttempt.Success(key)
          is Failure -> CreateReportAttempt.NoAttachments(key)
        }
      }
      is Failure -> return CreateReportAttempt.Failure
    }
  }

  suspend fun addAttachments(issueKey: IssueKey, attachments: Set<AttachmentBody>): Response<Unit> {
    val bodies = attachments.map(AttachmentBody::part)
    return jiraApi.attachFiles(issueKey, bodies)
  }

  private fun createNewIssueRequest(report: NewIssue): NewIssueRequest {
    val description = listOfNotNull(report.description, config.runtimeInfo, report.mentions)
        .joinToString("\n\n", transform = Describable::describe)

    return NewIssueRequest(
        NewIssueFieldsRequest(
            ProjectRequest(config.projectKey),
            IssueTypeRequest(report.issueType.id),
            report.summary.value,
            ReporterRequest(report.reporter.accountId),
            description,
            report.epic?.id
        )
    )
  }

  private fun createAddCommentRequest(report: AddComment): AddCommentRequest {
    val body = listOfNotNull(
        report.reporter,
        report.description,
        config.runtimeInfo,
        report.mentions
    ).joinToString("\n\n", transform = Describable::describe)
    return AddCommentRequest(body)
  }

  private suspend fun getEpics(): List<Epic> {
    return when (val response = jiraApi.getEpics(EpicJql(config.projectKey))) {
      is Success -> response.value.issues.map {
        Epic(it.key, it.fields.epicName)
      }
      is Failure -> emptyList()
    }
  }

  private suspend fun getIssuesTypesAndRoleIds(): Pair<List<IssueType>, List<String>>? {
    val (issueTypes, roleIds) = when (val response = jiraApi.getProject(config.projectKey)) {
      is Success -> response.value.issueTypes to response.value.roleIds
      is Failure -> return null
    }
    return issueTypes
        .filterNot { it.isSubTask }
        .map { IssueType(it.id, it.name) }
        .filter(config::filterIssuesTypes) to roleIds
  }

  private suspend fun getUsers(roleIds: List<String>): List<User> = coroutineScope {
    return@coroutineScope roleIds
        .map { roleId -> async { jiraApi.getUsers(config.projectKey, roleId) } }
        .awaitAll()
        .filterIsInstance<Success<RoleResponse>>()
        .flatMap { it.value.actors }
        .mapNotNull { actor -> actor.actorUser?.let { User(actor.displayName, it.accountId) } }
        .distinct()
        .filter(config::filterUser)
  }
}
