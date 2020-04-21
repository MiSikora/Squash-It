package io.mehow.squashit.report.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.mehow.squashit.R
import io.mehow.squashit.report.IssueKey
import io.mehow.squashit.report.ReportActivity
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.SubmitState.FailedToAttach
import io.mehow.squashit.report.SubmitState.Reattaching
import io.mehow.squashit.report.api.AttachmentBody
import io.mehow.squashit.report.extensions.activity
import io.mehow.squashit.report.extensions.clicks
import io.mehow.squashit.report.extensions.hideProgress
import io.mehow.squashit.report.extensions.viewScope
import io.mehow.squashit.report.presentation.Event.Reattach
import io.mehow.squashit.report.presentation.ReportPresenter
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class FailedToAttachView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : ConstraintLayout(context, attrs) {
  private lateinit var reportedIssueInfo: TextView
  private lateinit var goBack: Button
  private lateinit var retry: Button
  private var retryInput: RetryInput? = null
  private var initFailure = true

  override fun onFinishInflate() {
    super.onFinishInflate()
    reportedIssueInfo = findViewById(R.id.createdReportInfo)
    goBack = findViewById(R.id.goBack)
    retry = findViewById(R.id.retry)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    presenter.uiModels
      .map { it.submitState }
      .onEach { renderSubmitState(it) }
      .launchIn(viewScope)
    retry.clicks
      .filter { retry.isActivated }
      .mapNotNull { retryInput }
      .onEach { (key, files) -> presenter.sendEvent(Reattach(key, files)) }
      .launchIn(viewScope)
    goBack.setOnClickListener { activity.onBackPressed() }
  }

  private fun renderSubmitState(state: SubmitState) {
    if (state is FailedToAttach) {
      reportedIssueInfo.text =
        resources.getString(R.string.squash_it_reported, state.key.value)
      retryInput = RetryInput(state.key, state.attachments)
      if (initFailure) initFailure = false
      else (activity as ReportActivity).showSnackbar(resources.getString(R.string.squash_it_error))
    }
    val isRetrying = state is Reattaching
    retry.isActivated = !isRetrying
    if (isRetrying) retry.hideProgress(R.string.squash_it_retrying)
    else retry.hideProgress(R.string.squash_it_retry)
  }

  private data class RetryInput(val issueKey: IssueKey, val attachments: Set<AttachmentBody>)
}
