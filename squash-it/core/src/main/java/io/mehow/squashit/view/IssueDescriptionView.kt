package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.mehow.squashit.Description
import io.mehow.squashit.R
import io.mehow.squashit.extensions.textChanges
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.Event.SetDescription
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class IssueDescriptionView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : FrameLayout(context, attrs) {
  private val descriptionLayout: TextInputLayout
  val descriptionInput: TextInputEditText

  init {
    LayoutInflater.from(context).inflate(R.layout.issue_description, this, true)
    descriptionLayout = findViewById(R.id.descriptionLayout)
    descriptionInput = findViewById(R.id.descriptionInput)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    descriptionInput.textChanges
        .debounce(200)
        .map { it.trim() }
        .onEach { presenter.sendEvent(SetDescription(Description(it))) }
        .launchIn(viewScope)
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    showDescription(uiModel.issueDescription)
  }

  private fun showDescription(description: Description?) {
    if (description == null) return
    if (descriptionInput.text.isNullOrBlank()) descriptionInput.setText(description.value)
  }
}
