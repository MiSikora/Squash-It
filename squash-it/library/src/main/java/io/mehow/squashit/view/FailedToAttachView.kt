package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import io.mehow.squashit.IssueKey
import io.mehow.squashit.R
import io.mehow.squashit.SubmitState
import io.mehow.squashit.SubmitState.FailedToAttach
import io.mehow.squashit.SubmitState.Reattaching
import io.mehow.squashit.api.AttachmentBody
import io.mehow.squashit.extensions.activity
import io.mehow.squashit.extensions.clicks
import io.mehow.squashit.extensions.hideProgress
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.Event.Reattach
import io.mehow.squashit.presentation.ReportPresenter
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
  private lateinit var goBackButton: Button
  private lateinit var retryButton: Button
  private var retryInput: RetryInput? = null
  private var initFailure = true

  override fun onFinishInflate() {
    super.onFinishInflate()
    reportedIssueInfo = findViewById(R.id.createdReportInfo)
    goBackButton = findViewById(R.id.goBackButton)
    retryButton = findViewById(R.id.retryButton)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    presenter.uiModels
        .map { it.submitState }
        .onEach { renderSubmitState(it) }
        .launchIn(viewScope)
    retryButton.clicks
        .filter { retryButton.isActivated }
        .mapNotNull { retryInput }
        .onEach { (key, files) -> presenter.sendEvent(Reattach(key, files)) }
        .launchIn(viewScope)
    goBackButton.setOnClickListener { activity.onBackPressed() }
  }

  private fun renderSubmitState(state: SubmitState) {
    if (state is FailedToAttach) {
      reportedIssueInfo.text = resources.getString(R.string.squash_it_reported, state.key.value)
      retryInput = RetryInput(state.key, state.attachments)
      if (initFailure) initFailure = false
      else Toast.makeText(context, R.string.squash_it_error, Toast.LENGTH_LONG).show()
    }
    val isRetrying = state is Reattaching
    retryButton.isActivated = !isRetrying
    if (isRetrying) retryButton.hideProgress(R.string.squash_it_retrying)
    else retryButton.hideProgress(R.string.squash_it_retry)
  }

  private data class RetryInput(val issueKey: IssueKey, val attachments: Set<AttachmentBody>)
}
