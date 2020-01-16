package io.mehow.squashit.report.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.Button
import android.widget.CheckBox
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
import io.mehow.squashit.R
import io.mehow.squashit.report.ReportType
import io.mehow.squashit.report.ReportType.AddCommentToIssue
import io.mehow.squashit.report.ReportType.CreateNewIssue
import io.mehow.squashit.report.SubmitState.Submitting
import io.mehow.squashit.report.extensions.checkChanges
import io.mehow.squashit.report.extensions.clicks
import io.mehow.squashit.report.extensions.hideProgress
import io.mehow.squashit.report.extensions.showProgress
import io.mehow.squashit.report.extensions.viewScope
import io.mehow.squashit.report.presentation.Event.SubmitReport
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.UiModel
import io.mehow.squashit.report.presentation.UserInput
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
  private val newIssueCheckBox: CheckBox
  private val submit: Button

  private var userInput: UserInput? = null

  init {
    overScrollMode = OVER_SCROLL_NEVER
    hideKeyboardOnScroll()

    LayoutInflater.from(context).inflate(R.layout.issue, this, true)
    content = findViewById(R.id.issueContent)
    newIssueCheckBox = findViewById(R.id.newIssueCheckBox)
    submit = findViewById(R.id.submit)

    setUpInsets()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    requestApplyInsets()
    displayIssueType(newIssueCheckBox.isChecked, false)
    newIssueCheckBox.checkChanges
        .map { isChecked -> if (isChecked) CreateNewIssue else AddCommentToIssue }
        .onEach { presenter.sendEvent(UpdateInput.reportType(it)) }
        .launchIn(viewScope)
    submit.clicks
        .filter { submit.isActivated }
        .mapNotNull { userInput }
        .onEach { presenter.sendEvent(SubmitReport(it)) }
        .launchIn(viewScope)
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    val input = uiModel.input
    userInput = input
    selectIssueType(input.reportType)
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
    val isNewIssue = reportType == CreateNewIssue
    displayIssueType(isNewIssue, true)
    if (newIssueCheckBox.isChecked != isNewIssue) newIssueCheckBox.isChecked = isNewIssue
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
    setVisibility(R.id.newIssue, VISIBLE)
    setVisibility(R.id.issueId, GONE)
  }
  private val updateIssueConstraint = ConstraintSet().apply {
    clone(content)
    setVisibility(R.id.newIssue, GONE)
    setVisibility(R.id.issueId, VISIBLE)
  }
  private val transition = AutoTransition().apply {
    ordering = ORDERING_TOGETHER
    duration = 250L
    interpolator = AccelerateDecelerateInterpolator()
  }

  private fun displayIssueType(newIssue: Boolean, animate: Boolean) {
    val constraints = if (newIssue) newIssueConstraint else updateIssueConstraint
    if (animate) TransitionManager.beginDelayedTransition(content, transition)
    constraints.applyTo(content)
  }
}
