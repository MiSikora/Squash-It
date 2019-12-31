package io.mehow.squashit.report

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import io.mehow.squashit.BaseActivity
import io.mehow.squashit.FileParceler
import io.mehow.squashit.R
import io.mehow.squashit.SquashItLogger
import io.mehow.squashit.report.ReportConfig.Valid
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

internal class ReportActivity : BaseActivity() {
  private val mainScope = MainScope()
  private lateinit var presenter: ReportPresenter
  private lateinit var inflaterFactory: LayoutInflater.Factory2
  private lateinit var content: CoordinatorLayout
  private var snackbar: Snackbar? = null

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    val (config, screenshot) = intent.getParcelableExtra<Args>(ArgsKey)!!
    presenter = startPresenter(config.toServiceConfig(), screenshot)
    inflaterFactory = ReportInflaterFactory(layoutInflater.factory2, presenter)
    window.decorView.enableEdgeToEdgeAndNightMode()
    setContentView(R.layout.squash_it)
    content = findViewById(R.id.activityContent)
    presenter.uiModels
        .map { getLayoutId(it) }
        .distinctUntilChanged()
        .onEach { switchDisplayedLayout(it) }
        .launchIn(mainScope)
  }

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
    mainScope.cancel()
    if (!isChangingConfigurations) presenter.stop()
  }

  override fun onRetainCustomNonConfigurationInstance() = presenter

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

  override fun onCreateView(name: String, ctx: Context, attrs: AttributeSet): View? {
    return inflaterFactory.onCreateView(name, ctx, attrs)
  }

  override fun onCreateView(parent: View?, name: String, ctx: Context, attrs: AttributeSet): View? {
    return inflaterFactory.onCreateView(parent, name, ctx, attrs)
  }

  private fun startPresenter(config: ServiceConfig, screenshot: File?): ReportPresenter {
    @Suppress("DEPRECATION")
    val cachedPresenter = lastCustomNonConfigurationInstance as? ReportPresenter
    if (cachedPresenter != null) return cachedPresenter

    val presenter = ReportPresenter.create(
        config,
        filesDir,
        { screenshot },
        { withContext(Dispatchers.IO) { SquashItLogger.createLogFile(this@ReportActivity) } }
    )
    presenter.start(Dispatchers.Unconfined)
    return presenter
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode != RESULT_OK) return

    if (requestCode == AttachmentFactory.RequestCode) {
      val uri = data?.data ?: return
      val item = AttachmentFactory.create(contentResolver, uri)
      if (item != null) mainScope.launch { presenter.sendEvent(UpdateInput.attach(item)) }
      else showSnackbar(getString(R.string.squash_it_error))
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
  internal data class Args(
    val config: Valid,
    val screenshotFile: File?
  ) : Parcelable
}
