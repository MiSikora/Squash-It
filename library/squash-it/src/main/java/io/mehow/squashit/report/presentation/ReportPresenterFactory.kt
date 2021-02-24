package io.mehow.squashit.report.presentation

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.JiraService
import io.mehow.squashit.report.ProjectInfoStore
import io.mehow.squashit.report.api.EpicFieldsResponse
import io.mehow.squashit.report.api.JiraApi
import io.mehow.squashit.report.api.NewIssueFieldsRequest
import io.mehow.squashit.report.api.adapter.EpicFieldsResponseJsonAdapter
import io.mehow.squashit.report.api.adapter.NewIssueFieldsRequestJsonAdapter
import java.io.File
import java.lang.reflect.Type

internal class ReportPresenterFactory(
  private val config: SquashItConfig,
  private val projectInfoDir: File,
  private val screenshotFileProvider: suspend () -> File?,
  private val logFileProvider: suspend () -> File?,
) {
  fun create(): ReportPresenter {
    val moshi = Moshi.Builder()
        .add(EpicJsonFactory())
        .add(KotlinJsonAdapterFactory())
        .build()
    val projectInfoStore = ProjectInfoStore(projectInfoDir, moshi)
    val jiraApi = JiraApi.create(moshi, config)
    val jiraService = JiraService(config, projectInfoStore, jiraApi)
    return ReportPresenter(jiraService, screenshotFileProvider, logFileProvider)
  }

  private inner class EpicJsonFactory : JsonAdapter.Factory {
    override fun create(
      type: Type,
      annotations: MutableSet<out Annotation>,
      moshi: Moshi,
    ): JsonAdapter<*>? {
      return when (type) {
        EpicFieldsResponse::class.java -> EpicFieldsResponseJsonAdapter(
            epicFieldName = config.epicReadFieldName,
            moshi = moshi
        )
        NewIssueFieldsRequest::class.java -> NewIssueFieldsRequestJsonAdapter(
            epicFieldName = config.epicWriteFieldName,
            moshi = moshi
        )
        else -> null
      }
    }
  }
}
