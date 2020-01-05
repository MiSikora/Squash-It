package io.mehow.squashit.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
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
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import java.lang.reflect.Type

internal class ReportPresenterFactory(
  private val config: SquashItConfig,
  private val projectInfoDir: File,
  private val screenshotFileProvider: suspend () -> File?,
  private val logFileProvider: suspend () -> File?,
  private val dispatcher: CoroutineDispatcher
) : Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    val moshi = Moshi.Builder()
        .add(EpicJsonFactory(config))
        .add(KotlinJsonAdapterFactory())
        .build()
    val projectInfoStore = ProjectInfoStore(projectInfoDir, moshi)
    val jiraApi = JiraApi.create(moshi, config)
    val jiraService = JiraService(config, projectInfoStore, jiraApi)
    val presenter = ReportPresenter(jiraService, screenshotFileProvider, logFileProvider)
    presenter.start(dispatcher)
    @Suppress("UNCHECKED_CAST")
    return presenter as T
  }

  private class EpicJsonFactory(config: SquashItConfig) : JsonAdapter.Factory {
    private val readName = config.epicReadFieldName
    private val writeName = config.epicWriteFieldName
    override fun create(
      type: Type,
      annotations: MutableSet<out Annotation>,
      moshi: Moshi
    ): JsonAdapter<*>? {
      return when (type) {
        EpicFieldsResponse::class.java -> EpicFieldsResponseJsonAdapter(readName, moshi)
        NewIssueFieldsRequest::class.java -> NewIssueFieldsRequestJsonAdapter(writeName, moshi)
        else -> null
      }
    }
  }
}
