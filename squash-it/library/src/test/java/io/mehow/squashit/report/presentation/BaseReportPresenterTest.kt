package io.mehow.squashit.report.presentation

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mehow.squashit.report.AppInfo
import io.mehow.squashit.report.DeviceInfo
import io.mehow.squashit.report.Epic
import io.mehow.squashit.report.InitState
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.JiraService
import io.mehow.squashit.report.OsInfo
import io.mehow.squashit.report.ProjectInfo
import io.mehow.squashit.report.ProjectInfoStore
import io.mehow.squashit.report.ReportConfig
import io.mehow.squashit.report.RuntimeInfo
import io.mehow.squashit.report.User
import io.mehow.squashit.report.api.FakeJiraApi
import io.mehow.squashit.report.presentation.extensions.PresenterAssert
import io.mehow.squashit.report.presentation.extensions.test
import io.mehow.squashit.report.presentation.extensions.withInitState
import io.mehow.squashit.report.presentation.extensions.withProjectInfo
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Locale
import java.util.TimeZone

internal open class BaseReportPresenterTest {
  @get:Rule val folder = TemporaryFolder.builder().assureDeletion().build()

  private val testDispatcher = TestCoroutineDispatcher()
  lateinit var presenterFactory: ReportPresenterFactory

  @Before fun setUp() {
    presenterFactory = ReportPresenterFactory(folder.newFolder())
  }

  val idleModel = UiModel.Initial
  val syncedModel = idleModel
      .withInitState(InitState.Idle)
      .withProjectInfo(
          ProjectInfo(
              epics = setOf(Epic("Epic ID", "Epic Name")),
              users = setOf(User("User Name", "User ID")),
              issueTypes = setOf(IssueType("Issue ID", "Issue Name"))
          )
      )

  internal fun testPresenter(
    skipInitialization: Boolean = true,
    block: suspend PresenterAssert.() -> Unit
  ) = runBlockingTest {
    presenterFactory.create().test(testDispatcher) {
      if (skipInitialization) {
        expectItem()
        expectItem()
      }
      block()
    }
  }

  data class ReportPresenterFactory(
    val storageDir: File,
    val config: ReportConfig.Valid = ReportConfig.Valid(
        "SQ",
        "https://www.squash.it".toHttpUrl(),
        "email",
        "token",
        emptyList(),
        emptyList(),
        RuntimeInfo(
            AppInfo("version name", "version code", "package name"),
            DeviceInfo(
                "manufacturer",
                "model",
                "resolution",
                "density",
                listOf(Locale.US),
                TimeZone.getTimeZone("CEST")
            ),
            OsInfo("release", 100)
        ),
        epicReadFieldName = "customfield_10009",
        epicWriteFieldName = "customfield_10008"
    ),
    val screenshotFile: File? = null,
    val logsFile: File? = null
  ) {
    internal val jiraApi = FakeJiraApi()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jiraService = JiraService(
        config,
        ProjectInfoStore(storageDir, moshi),
        jiraApi
    )

    fun create(): ReportPresenter {
      return ReportPresenter(jiraService, { screenshotFile }, { logsFile })
    }
  }
}
