package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.mehow.squashit.InputError.NoIssueId
import io.mehow.squashit.IssueId
import io.mehow.squashit.IssueKey
import io.mehow.squashit.R
import io.mehow.squashit.extensions.focuses
import io.mehow.squashit.extensions.textChanges
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.Event.DismissError
import io.mehow.squashit.presentation.Event.SetIssueKey
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class UpdateIssueView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : FrameLayout(context, attrs) {
  private val projectKey = resources.getString(R.string.squash_it_jira_project_key)

  private val issueIdLayout: TextInputLayout
  private val issueIdInput: TextInputEditText

  init {
    LayoutInflater.from(context).inflate(R.layout.update_issue, this, true)
    issueIdLayout = findViewById(R.id.issueIdLayout)
    issueIdInput = findViewById(R.id.issueIdInput)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    issueIdInput.textChanges
        .debounce(200)
        .mapNotNull { it.trim().toLongOrNull() }
        .map { IssueKey("$projectKey-$it") }
        .onEach { presenter.sendEvent(SetIssueKey(it)) }
        .launchIn(viewScope)
    issueIdInput.focuses
        .onEach { presenter.sendEvent(DismissError(NoIssueId)) }
        .launchIn(viewScope)
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    val input = uiModel.input
    showIssueId(input.issueKey?.toIssueId())
    renderIssueIdError(NoIssueId in input.errors)
  }

  private fun renderIssueIdError(noIssueId: Boolean) {
    issueIdLayout.error = if (noIssueId) "Please provide issue ID" else ""
  }

  private fun showIssueId(issueId: IssueId?) {
    if (issueId == null) return
    val displayedId = "${issueIdInput.text}".toLongOrNull()
    if (displayedId != issueId.value) issueIdInput.setText("${issueId.value}")
  }
}
