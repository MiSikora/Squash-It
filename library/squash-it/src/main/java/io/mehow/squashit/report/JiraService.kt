package io.mehow.squashit.report

import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.api.AttachmentBody
import io.mehow.squashit.report.api.EpicJql
import io.mehow.squashit.report.api.JiraApi
import io.mehow.squashit.report.api.Response
import io.mehow.squashit.report.api.Response.Failure
import io.mehow.squashit.report.api.Response.Success
import io.mehow.squashit.report.api.RoleResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class JiraService(
  private val config: SquashItConfig,
  private val projectInfoStore: ProjectInfoStore,
  private val jiraApi: JiraApi
) {
  suspend fun report(report: Report): CreateReportAttempt {
    return report.toCall(config).execute(jiraApi)
  }

  suspend fun addAttachments(
    issueKey: IssueKey,
    attachments: Set<AttachmentBody>
  ): Response<Unit> {
    val bodies = attachments.map(AttachmentBody::part)
    return jiraApi.attachFiles(issueKey, bodies)
  }

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
      .filter(config.issueTypeFilter) to roleIds
  }

  private suspend fun getUsers(roleIds: List<String>): List<User> = coroutineScope {
    return@coroutineScope roleIds
      .map { roleId -> async { jiraApi.getUsers(config.projectKey, roleId) } }
      .awaitAll()
      .filterIsInstance<Success<RoleResponse>>()
      .flatMap { it.value.actors }
      .mapNotNull { actor -> actor.actorUser?.let { User(actor.displayName, it.accountId) } }
      .distinct()
      .filter(config.userFilter)
  }
}
