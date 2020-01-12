package io.mehow.squashit.report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import io.mehow.squashit.FileParceler
import io.mehow.squashit.NoTelescope
import io.mehow.squashit.R
import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.SquashItLogger
import io.mehow.squashit.report.SubmitState.AddedAttachments
import io.mehow.squashit.report.SubmitState.Failed
import io.mehow.squashit.report.SubmitState.FailedToAttach
import io.mehow.squashit.report.SubmitState.Idle
import io.mehow.squashit.report.SubmitState.Reattaching
import io.mehow.squashit.report.SubmitState.Resubmitting
import io.mehow.squashit.report.SubmitState.Submitted
import io.mehow.squashit.report.SubmitState.Submitting
import io.mehow.squashit.report.extensions.enableEdgeToEdgeAndNightMode
import io.mehow.squashit.report.presentation.Event.GoIdle
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.ReportPresenterFactory
import io.mehow.squashit.report.presentation.UiModel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class ReportActivity : Activity(), NoTelescope {
  private val mainScope = MainScope()
  private lateinit var presenter: ReportPresenter
  private lateinit var content: CoordinatorLayout
  private var snackbar: Snackbar? = null

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    presenter = lastNonConfigurationInstance
    layoutInflater.factory2 = ReportInflaterFactory(presenter)
    window.decorView.enableEdgeToEdgeAndNightMode()
    setContentView(R.layout.squash_it)
    content = findViewById(R.id.activityContent)
    presenter.uiModels
        .map { getLayoutId(it) }
        .distinctUntilChanged()
        .onEach { switchDisplayedLayout(it) }
        .launchIn(mainScope)
  }

  override fun getLastNonConfigurationInstance(): ReportPresenter {
    val screenshot = intent.getParcelableExtra<Args>(ArgsKey)!!.screenshotFile
    return super.getLastNonConfigurationInstance() as? ReportPresenter
        ?: createFactory(screenshot).create().also { it.start(Dispatchers.Unconfined) }
  }

  override fun onRetainNonConfigurationInstance(): ReportPresenter = presenter

  @Suppress("MaxLineLength")
  @LayoutRes private fun getLayoutId(uiModel: UiModel) = when (uiModel.initState) {
    InitState.Initializing -> R.layout.init_progress
    InitState.Failure -> R.layout.init_failure
    else -> when (uiModel.submitState) {
      is Idle, is Submitting -> R.layout.report
      is Submitted -> R.layout.created_report
      is FailedToAttach, is Reattaching -> R.layout.failed_to_attach
      is Failed, is Resubmitting -> R.layout.submit_failure
      is AddedAttachments -> R.layout.attachments_added
    }
  }

  private fun switchDisplayedLayout(@LayoutRes layoutId: Int) {
    if (content.isNotEmpty()) {
      content.removeAllViews()
      TransitionManager.beginDelayedTransition(content, Fade())
    }
    layoutInflater.inflate(layoutId, content, true)
  }

  override fun onDestroy() {
    super.onDestroy()
    mainScope.cancel("Report Activity destroyed.")
    if (!isChangingConfigurations) presenter.stop()
  }

  override fun onBackPressed() {
    val override = content.isNotEmpty() && !content[0].isEntryScreen
    if (override) {
      mainScope.launch { presenter.sendEvent(GoIdle) }
    } else {
      super.onBackPressed()
      overridePendingTransition(R.anim.no_op, R.anim.slide_down)
    }
  }

  private val View.isEntryScreen: Boolean
    get() {
      return id in listOf(R.id.initFailureRoot, R.id.initProgressRoot, R.id.reportRoot)
    }

  private fun createFactory(screenshot: File?): ReportPresenterFactory {
    val appContext = applicationContext
    return ReportPresenterFactory(
        SquashItConfig.Instance,
        filesDir,
        { screenshot },
        { withContext(Dispatchers.IO) { SquashItLogger.createLogFile(appContext) } }
    )
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode != RESULT_OK) return

    if (requestCode == AttachmentFactory.RequestCode) {
      val uri = data?.data ?: return
      mainScope.launch {
        val item = withContext(Dispatchers.IO) {
          AttachmentFactory.create(applicationContext.contentResolver, AttachmentId("$uri"))
        }
        if (item != null) mainScope.launch { presenter.sendEvent(UpdateInput.attach(item)) }
        else showSnackbar(getString(R.string.squash_it_error))
      }
    }
  }

  fun showSnackbar(text: String) {
    snackbar?.dismiss()
    snackbar = Snackbar.make(content, text, Snackbar.LENGTH_LONG)
    snackbar?.show()
  }

  companion object {
    private const val ArgsKey = "ReportActivity.Args"

    fun start(activity: Activity, args: Args) {
      val start = Intent(activity, ReportActivity::class.java).putExtra(ArgsKey, args)
      activity.startActivity(start)
      activity.overridePendingTransition(R.anim.slide_up, R.anim.no_op)
    }
  }

  @Parcelize
  @TypeParceler<File?, FileParceler>
  data class Args(val screenshotFile: File?) : Parcelable
}
