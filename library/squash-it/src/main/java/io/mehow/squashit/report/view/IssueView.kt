package io.mehow.squashit.report.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet.ORDERING_TOGETHER
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.mehow.squashit.R
import io.mehow.squashit.report.InputError.NoIssueType
import io.mehow.squashit.report.InputError.ShortSummary
import io.mehow.squashit.report.IssueType
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.ReportType.AddCommentToIssue
import io.mehow.squashit.report.ReportType.AddSubTaskToIssue
import io.mehow.squashit.report.ReportType.CreateNewIssue
import io.mehow.squashit.report.SubmitState.Submitting
import io.mehow.squashit.report.Summary
import io.mehow.squashit.report.extensions.checkChanges
import io.mehow.squashit.report.extensions.clicks
import io.mehow.squashit.report.extensions.focuses
import io.mehow.squashit.report.extensions.hideProgress
import io.mehow.squashit.report.extensions.showProgress
import io.mehow.squashit.report.extensions.textChanges
import io.mehow.squashit.report.extensions.viewScope
import io.mehow.squashit.report.presentation.Event.SubmitReport
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.UiModel
import io.mehow.squashit.report.presentation.UserInput
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlin.math.absoluteValue

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class IssueView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : NestedScrollView(context, attrs) {
  private val content: ConstraintLayout
  private val submit: Button
  private val updateGroup: RadioGroup
  private val issueTypeLayout: TextInputLayout
  private val issueTypeInput: AutoCompleteTextView
  private val summaryLayout: TextInputLayout
  private val summaryInput: TextInputEditText
  private var adapter = IssueTypeAdapter(context, emptyList())

  private var userInput: UserInput? = null

  init {
    overScrollMode = OVER_SCROLL_NEVER
    hideKeyboardOnScroll()

    LayoutInflater.from(context).inflate(R.layout.squash_it_issue, this, true)
    content = findViewById(R.id.issueContent)
    submit = findViewById(R.id.submit)
    updateGroup = findViewById(R.id.updateGroup)
    issueTypeLayout = findViewById(R.id.issueTypeLayout)
    issueTypeInput = findViewById(R.id.issueTypeInput)
    summaryLayout = findViewById(R.id.summaryLayout)
    summaryInput = findViewById(R.id.summaryInput)

    setUpInsets()
  }

  private val reportType: ReportType
    get() = when (val id = updateGroup.checkedRadioButtonId) {
      R.id.newIssue -> CreateNewIssue
      R.id.addComment -> AddCommentToIssue
      R.id.addSubTask -> AddSubTaskToIssue
      else -> error("Unexpected ID: ${resources.getResourceName(id)}")
    }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    requestApplyInsets()
    displayIssueType(reportType, false)
    emitReportTypeChanges()
    emitIssueTypeChanges()
    emitSummaryChanges()
    hideSummaryErrors()
    emitSubmissions()
    observeUiModels()
  }

  private fun emitReportTypeChanges() {
    updateGroup.checkChanges
      .map { reportType }
      .onEach { presenter.sendEvent(UpdateInput.reportType(it)) }
      .launchIn(viewScope)
  }

  private fun emitIssueTypeChanges() {
    issueTypeInput.textChanges
      .mapNotNull { text -> adapter.issueTypes.find { it.name == text } }
      .onEach { presenter.sendEvent(UpdateInput.issueType(it)) }
      .onEach { presenter.sendEvent(UpdateInput.hideError(NoIssueType)) }
      .launchIn(viewScope)
  }

  private fun emitSummaryChanges() {
    summaryInput.textChanges
      .debounce(200)
      .map { it.trim() }
      .onEach { presenter.sendEvent(UpdateInput.summary(Summary(it))) }
      .launchIn(viewScope)
  }

  private fun hideSummaryErrors() {
    summaryInput.focuses
      .onEach { presenter.sendEvent(UpdateInput.hideError(ShortSummary)) }
      .launchIn(viewScope)
  }

  private fun observeUiModels() {
    presenter.uiModels
      .onEach { renderUiModel(it) }
      .launchIn(viewScope)
  }

  private fun emitSubmissions() {
    submit.clicks
      .filter { submit.isActivated }
      .mapNotNull { userInput }
      .onEach { presenter.sendEvent(SubmitReport(it)) }
      .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    val input = uiModel.input
    userInput = input
    selectIssueType(input.reportType)
    createIssueTypes(uiModel.projectInfo?.issueTypes.orEmpty(), input.type)
    showSummary(input.summary)
    renderIssueTypeError(NoIssueType in input.errors)
    renderSummaryError(ShortSummary in input.errors)
    val isInitialized = uiModel.projectInfo != null
    val isSubmitting = uiModel.submitState == Submitting
    submit.isActivated = isInitialized && !isSubmitting
    when {
      !isInitialized -> submit.showProgress(R.string.squash_it_initializing)
      isSubmitting -> submit.showProgress(R.string.squash_it_submitting)
      else -> submit.hideProgress(R.string.squash_it_submit)
    }
  }

  private fun selectIssueType(reportType: ReportType) {
    displayIssueType(reportType, true)
    val id = reportType.buttonId
    val button = updateGroup.findViewById<RadioButton>(id)
    if (!button.isChecked) button.isChecked = true
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

  private fun hideKeyboardOnScroll() {
    val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var totalDy = 0
    onScroll { _, dy ->
      totalDy += dy.absoluteValue
      if (totalDy >= touchSlop) {
        totalDy = 0
        val windowToken = content.windowToken
        val inputMethodManager = context.getSystemService<InputMethodManager>()!!
        inputMethodManager.hideSoftInputFromWindow(windowToken, HIDE_NOT_ALWAYS)
      }
    }
  }

  private fun onScroll(body: (dx: Int, dy: Int) -> Unit) {
    setOnScrollChangeListener(OnScrollChangeListener { _, dx, dy, _, _ -> body(dx, dy) })
  }

  private fun setUpInsets() {
    val bottomRootPadding = content.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
      view.updatePadding(bottom = bottomRootPadding + insets.systemWindowInsetBottom)
      return@setOnApplyWindowInsetsListener insets
    }
  }

  private val newIssueConstraint = ConstraintSet().apply {
    clone(content)
    setVisibility(R.id.summaryLayout, VISIBLE)
    setVisibility(R.id.issueTypeLayout, VISIBLE)
    setVisibility(R.id.issueId, GONE)
  }
  private val addCommentConstraint = ConstraintSet().apply {
    clone(content)
    setVisibility(R.id.summaryLayout, GONE)
    setVisibility(R.id.issueTypeLayout, GONE)
    setVisibility(R.id.issueId, VISIBLE)
  }
  private val addSubTaskConstraint = ConstraintSet().apply {
    clone(content)
    setVisibility(R.id.summaryLayout, VISIBLE)
    setVisibility(R.id.issueTypeLayout, GONE)
    setVisibility(R.id.issueId, VISIBLE)
  }
  private val transition = AutoTransition().apply {
    ordering = ORDERING_TOGETHER
    duration = 250L
    interpolator = AccelerateDecelerateInterpolator()
  }

  private fun displayIssueType(reportType: ReportType, animate: Boolean) {
    val constraints = when (reportType) {
      CreateNewIssue -> newIssueConstraint
      AddCommentToIssue -> addCommentConstraint
      AddSubTaskToIssue -> addSubTaskConstraint
    }
    if (animate) TransitionManager.beginDelayedTransition(content, transition)
    constraints.applyTo(content)
  }

  private class IssueTypeAdapter(
    context: Context,
    val issueTypes: List<IssueType>
  ) : ArrayAdapter<String>(context, R.layout.squash_it_select_text_view) {
    override fun getCount() = issueTypes.size
    override fun getItem(position: Int) = issueTypes[position].name
  }

  private val ReportType.buttonId: Int
    get() = when (this) {
      CreateNewIssue -> R.id.newIssue
      AddCommentToIssue -> R.id.addComment
      AddSubTaskToIssue -> R.id.addSubTask
    }
}
