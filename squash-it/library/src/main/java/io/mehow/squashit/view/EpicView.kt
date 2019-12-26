package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import io.mehow.squashit.Epic
import io.mehow.squashit.R
import io.mehow.squashit.ReportType
import io.mehow.squashit.ReportType.CreateNewIssue
import io.mehow.squashit.extensions.textChanges
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.Event.UpdateInput
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class EpicView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : FrameLayout(context, attrs) {
  private val epicLayout: TextInputLayout
  private val epicInput: AutoCompleteTextView
  private var adapter = EpicAdapter(context, emptyList())

  init {
    LayoutInflater.from(context).inflate(R.layout.epic, this, true)
    epicLayout = findViewById(R.id.epicLayout)
    epicInput = findViewById(R.id.epicInput)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    epicInput.textChanges
        .mapNotNull { text -> adapter.epics.find { it.name == text } }
        .onEach { presenter.sendEvent(UpdateInput.epic(it)) }
        .launchIn(viewScope)
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    val input = uiModel.input
    createEpics(uiModel.projectInfo?.epics.orEmpty(), input.newIssue.epic, input.reportType)
  }

  private fun createEpics(epics: Set<Epic>, epic: Epic?, reportType: ReportType) {
    isVisible = epics.isNotEmpty() && reportType == CreateNewIssue

    val adapter = if (adapter.epics == epics) adapter
    else EpicAdapter(context, epics.sortedBy { it.name })
    if (adapter != this.adapter) {
      this.adapter = adapter
      epicInput.setAdapter(adapter)
    }

    if (epic == null) return
    if ("${epicInput.text}" != epic.name) epicInput.setText(epic.name)
  }

  private class EpicAdapter(
    context: Context,
    val epics: List<Epic>
  ) : ArrayAdapter<String>(context, R.layout.select_text_view) {
    override fun getCount() = epics.size
    override fun getItem(position: Int) = epics[position].name
  }
}
