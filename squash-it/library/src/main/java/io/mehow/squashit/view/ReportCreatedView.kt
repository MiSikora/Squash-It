package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import io.mehow.squashit.R
import io.mehow.squashit.SubmitState.Submitted
import io.mehow.squashit.extensions.activity
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.ReportPresenter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class ReportCreatedView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : LinearLayout(context, attrs) {
  private lateinit var reportedIssueInfo: TextView
  private lateinit var goBackButton: Button

  init {
    orientation = VERTICAL
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    reportedIssueInfo = findViewById(R.id.createdReportInfo)
    goBackButton = findViewById(R.id.goBackButton)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    presenter.uiModels
        .map { it.submitState }
        .filterIsInstance<Submitted>()
        .onEach { submitState ->
          val text = resources.getString(R.string.squash_it_reported, submitState.key.value)
          reportedIssueInfo.text = text
        }
        .launchIn(viewScope)
    goBackButton.setOnClickListener { activity.onBackPressed() }
  }
}
