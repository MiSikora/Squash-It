package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputLayout
import io.mehow.squashit.InputError.NoReporter
import io.mehow.squashit.R
import io.mehow.squashit.User
import io.mehow.squashit.extensions.clicks
import io.mehow.squashit.extensions.textChanges
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.Event.DismissError
import io.mehow.squashit.presentation.Event.SetReporter
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class ReporterView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : FrameLayout(context, attrs) {
  private val reporterLayout: TextInputLayout
  private val reporterInput: AutoCompleteTextView
  private var adapter = ReporterAdapter(context, emptyList())

  init {
    LayoutInflater.from(context).inflate(R.layout.reporter, this, true)
    reporterLayout = findViewById(R.id.reporterLayout)
    reporterInput = findViewById(R.id.reporterInput)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    reporterInput.textChanges
        .mapNotNull { text -> adapter.users.find { it.nameHandle == text } }
        .onEach { presenter.sendEvent(SetReporter(it)) }
        .launchIn(viewScope)
    reporterInput.clicks
        .onEach { presenter.sendEvent(DismissError(NoReporter)) }
        .launchIn(viewScope)
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    createReporters(uiModel.projectInfo?.users.orEmpty(), uiModel.reporter)
    renderError(NoReporter in uiModel.errors)
  }

  private fun createReporters(users: Set<User>, reporter: User?) {
    val adapter = if (adapter.users == users) adapter
    else ReporterAdapter(context, users.sortedBy { it.nameHandle })
    if (adapter != this.adapter) {
      this.adapter = adapter
      reporterInput.setAdapter(adapter)
    }

    if (reporter == null) return
    if ("${reporterInput.text}" != reporter.nameHandle) reporterInput.setText(reporter.nameHandle)
  }

  private fun renderError(noReporter: Boolean) {
    reporterLayout.error = if (noReporter) "Please add reporter" else ""
  }

  private class ReporterAdapter(
    context: Context,
    val users: List<User>
  ) : ArrayAdapter<String>(context, R.layout.select_text_view) {
    override fun getCount() = users.size
    override fun getItem(position: Int) = users[position].nameHandle
  }
}
