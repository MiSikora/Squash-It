package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.mehow.squashit.InputError.NoIssueType
import io.mehow.squashit.InputError.ShortSummary
import io.mehow.squashit.IssueType
import io.mehow.squashit.R
import io.mehow.squashit.Summary
import io.mehow.squashit.extensions.clicks
import io.mehow.squashit.extensions.focuses
import io.mehow.squashit.extensions.textChanges
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.Event.DismissError
import io.mehow.squashit.presentation.Event.SetIssueType
import io.mehow.squashit.presentation.Event.SetSummary
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class NewIssueView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : LinearLayout(context, attrs) {
  private val issueTypeLayout: TextInputLayout
  private val issueTypeInput: AutoCompleteTextView
  private val summaryLayout: TextInputLayout
  private val summaryInput: TextInputEditText
  private var adapter = IssueTypeAdapter(context, emptyList())

  init {
    orientation = VERTICAL

    LayoutInflater.from(context).inflate(R.layout.new_issue, this, true)
    issueTypeLayout = findViewById(R.id.issueTypeLayout)
    issueTypeInput = findViewById(R.id.issueTypeInput)
    summaryLayout = findViewById(R.id.summaryLayout)
    summaryInput = findViewById(R.id.summaryInput)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    emitIssueTypeChanges()
    emitSummaryChanges()
    hideIssueTypeErrors()
    hideSummaryErrors()
    observeUiModels()
  }

  private fun emitIssueTypeChanges() {
    issueTypeInput.textChanges
        .mapNotNull { text -> adapter.issueTypes.find { it.name == text } }
        .onEach { presenter.sendEvent(SetIssueType(it)) }
        .launchIn(viewScope)
  }

  private fun emitSummaryChanges() {
    summaryInput.textChanges
        .debounce(200)
        .map { it.trim() }
        .onEach { presenter.sendEvent(SetSummary(Summary(it))) }
        .launchIn(viewScope)
  }

  private fun hideIssueTypeErrors() {
    issueTypeInput.clicks
        .onEach { presenter.sendEvent(DismissError(NoIssueType)) }
        .launchIn(viewScope)
  }

  private fun hideSummaryErrors() {
    summaryInput.focuses
        .onEach { presenter.sendEvent(DismissError(ShortSummary)) }
        .launchIn(viewScope)
  }

  private fun observeUiModels() {
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    val input = uiModel.input
    createIssueTypes(uiModel.projectInfo?.issueTypes.orEmpty(), input.newIssue.type)
    showSummary(input.newIssue.summary)
    renderIssueTypeError(NoIssueType in input.errors)
    renderSummaryError(ShortSummary in input.errors)
  }

  private fun createIssueTypes(issueTypes: Set<IssueType>, issueType: IssueType?) {
    val adapter = if (adapter.issueTypes == issueTypes) adapter
    else IssueTypeAdapter(context, issueTypes.sortedBy { it.name })

    if (adapter != this.adapter) {
      this.adapter = adapter
      issueTypeInput.setAdapter(adapter)
    }

    if (issueType == null) return
    if ("${issueTypeInput.text}" != issueType.name) issueTypeInput.setText(issueType.name)
  }

  private fun showSummary(summary: Summary?) {
    if (summary == null) return
    if (summaryInput.text.isNullOrBlank()) summaryInput.setText(summary.value)
  }

  private fun renderIssueTypeError(noIssueType: Boolean) {
    issueTypeLayout.error = if (noIssueType) "Please add issue type" else ""
  }

  private fun renderSummaryError(shortSummary: Boolean) {
    summaryLayout.error = if (shortSummary) "Summary must be at least 10 characters long" else ""
  }

  private class IssueTypeAdapter(
    context: Context,
    val issueTypes: List<IssueType>
  ) : ArrayAdapter<String>(context, R.layout.select_text_view) {
    override fun getCount() = issueTypes.size
    override fun getItem(position: Int) = issueTypes[position].name
  }
}
