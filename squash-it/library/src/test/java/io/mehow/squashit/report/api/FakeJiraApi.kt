package io.mehow.squashit.report.api

import io.mehow.squashit.report.IssueKey
import kotlinx.coroutines.flow.asFlow
import okhttp3.MultipartBody.Part

internal class FakeJiraApi : JiraApi {
  val epicsFactory = EpicsFactory()
  val projectFactory = ProjectFactory()
  val roleFactory = RoleFactory()
  val newIssueFactory = NewIssueFactory()
  val commentFactory = UnitFactory()
  val attachmentsFactory = UnitFactory()

  private val _newIssueRecords = mutableListOf<NewIssueRecord>()
  val newIssueRecords get() = _newIssueRecords.asFlow()
  private val _addCommentRecords = mutableListOf<AddCommentRecord>()
  val addCommentRecords = _addCommentRecords.asFlow()

  override suspend fun getEpics(epicJql: EpicJql): Response<EpicsResponse> {
    return epicsFactory.create()
  }

  override suspend fun getProject(projectKey: String): Response<ProjectResponse> {
    return projectFactory.create()
  }

  override suspend fun getUsers(projectKey: String, roleId: String): Response<RoleResponse> {
    return roleFactory.create()
  }

  override suspend fun createNewIssue(request: NewIssueRequest): Response<CreateNewIssueResponse> {
    _newIssueRecords.add(NewIssueRecord(request))
    return newIssueFactory.create()
  }

  override suspend fun addComment(issueKey: IssueKey, request: AddCommentRequest): Response<Unit> {
    _addCommentRecords.add(AddCommentRecord(issueKey, request))
    return commentFactory.create()
  }

  override suspend fun attachFiles(issueKey: IssueKey, files: List<Part>): Response<Unit> {
    return attachmentsFactory.create()
  }

  data class NewIssueRecord(val request: NewIssueRequest)
  data class AddCommentRecord(val issueKey: IssueKey, val request: AddCommentRequest)
}
