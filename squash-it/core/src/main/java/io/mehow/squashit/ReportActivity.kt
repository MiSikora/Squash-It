package io.mehow.squashit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.transition.Fade
import androidx.transition.TransitionManager
import io.mehow.squashit.SubmitState.AddedAttachments
import io.mehow.squashit.SubmitState.AddedComment
import io.mehow.squashit.SubmitState.CreatedNew
import io.mehow.squashit.SubmitState.Failed
import io.mehow.squashit.SubmitState.FailedToAttachForComment
import io.mehow.squashit.SubmitState.FailedToAttachForNew
import io.mehow.squashit.SubmitState.Idle
import io.mehow.squashit.SubmitState.RetryingAttachmentsForComment
import io.mehow.squashit.SubmitState.RetryingAttachmentsForNew
import io.mehow.squashit.SubmitState.RetryingSubmission
import io.mehow.squashit.SubmitState.Submitting
import io.mehow.squashit.extensions.enableEdgeToEdgeAndNightMode
import io.mehow.squashit.presentation.Event.AddAttachment
import io.mehow.squashit.presentation.Event.GoIdle
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import kotlinx.android.parcel.Parceler
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

internal class ReportActivity : AppCompatActivity() {
  private val mainScope = MainScope()
  private lateinit var presenter: ReportPresenter
  private lateinit var inflaterFactory: LayoutInflater.Factory2
  private lateinit var content: FrameLayout

  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    val (config, screenshot) = intent.getParcelableExtra<Args>(ArgsKey)!!
    presenter = startPresenter(config.toServiceConfig(), screenshot)
    inflaterFactory = SquashItInflaterFactory(layoutInflater.factory2, presenter)
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
      Idle, Submitting -> R.layout.report
      is CreatedNew -> R.layout.new_issue_created
      is FailedToAttachForNew, RetryingAttachmentsForNew -> R.layout.new_issue_created_without_attachments
      is AddedComment -> R.layout.comment_added
      is FailedToAttachForComment, RetryingAttachmentsForComment -> R.layout.comment_added_without_attachments
      is Failed, RetryingSubmission -> R.layout.submit_failure
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

    if (requestCode == AttachmentItemFactory.RequestCode) {
      val uri = data?.data ?: return
      val item = AttachmentItemFactory.create(contentResolver, uri)
      if (item != null) mainScope.launch { presenter.sendEvent(AddAttachment(item)) }
      else Toast.makeText(this, R.string.squash_it_error, LENGTH_LONG).show()
    }
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
    val config: SquashItConfig.Valid,
    val screenshotFile: File?
  ) : Parcelable

  object FileParceler : Parceler<File?> {
    override fun create(parcel: Parcel) = parcel.readString()?.let { File(it) }
    override fun File?.write(parcel: Parcel, flags: Int) = parcel.writeString(this?.path)
  }
}
