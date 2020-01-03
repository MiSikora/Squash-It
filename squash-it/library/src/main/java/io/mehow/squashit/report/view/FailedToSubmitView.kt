package io.mehow.squashit.report.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import io.mehow.squashit.R
import io.mehow.squashit.report.Report
import io.mehow.squashit.report.ReportActivity
import io.mehow.squashit.report.SubmitState
import io.mehow.squashit.report.SubmitState.Failed
import io.mehow.squashit.report.SubmitState.Resubmitting
import io.mehow.squashit.report.extensions.activity
import io.mehow.squashit.report.extensions.clicks
import io.mehow.squashit.report.extensions.hideProgress
import io.mehow.squashit.report.extensions.showProgress
import io.mehow.squashit.report.extensions.viewScope
import io.mehow.squashit.report.presentation.Event.RetrySubmission
import io.mehow.squashit.report.presentation.ReportPresenter
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class FailedToSubmitView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : ConstraintLayout(context, attrs) {
  private lateinit var goBack: Button
  private lateinit var retry: Button
  private var report: Report? = null
  private var initFailure = true

  override fun onFinishInflate() {
    super.onFinishInflate()
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
        .mapNotNull { report }
        .onEach { presenter.sendEvent(RetrySubmission(it)) }
        .launchIn(viewScope)
    goBack.setOnClickListener { activity.onBackPressed() }
  }

  private fun renderSubmitState(state: SubmitState) {
    if (state is Failed) {
      report = state.report
      if (initFailure) initFailure = false
      else (activity as ReportActivity).showSnackbar(resources.getString(R.string.squash_it_error))
    }
    val isRetrying = state is Resubmitting
    retry.isActivated = !isRetrying
    if (isRetrying) retry.showProgress(R.string.squash_it_retrying)
    else retry.hideProgress(R.string.squash_it_retry)
  }
}
