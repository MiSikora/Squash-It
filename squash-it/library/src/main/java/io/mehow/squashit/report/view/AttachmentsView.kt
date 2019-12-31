package io.mehow.squashit.report.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import io.mehow.squashit.R
import io.mehow.squashit.report.AttachState
import io.mehow.squashit.report.AttachState.Attach
import io.mehow.squashit.report.AttachState.DoNotAttach
import io.mehow.squashit.report.AttachState.Unavailable
import io.mehow.squashit.report.AttachmentFactory
import io.mehow.squashit.report.extensions.activity
import io.mehow.squashit.report.extensions.checkChanges
import io.mehow.squashit.report.extensions.viewScope
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.UiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class AttachmentsView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : ConstraintLayout(context, attrs) {
  private val screenshotCheckBox: CheckBox
  private val logsCheckBox: CheckBox
  private val addAttachmentButton: Button
  private val additionalAttachments: RecyclerView
  private var screenshot: File? = null
  private var logs: File? = null

  private val attachmentsAdapter = AttachmentAdapter(LayoutInflater.from(context)) {
    viewScope.launch { presenter.sendEvent(UpdateInput.detach(it)) }
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.attachments, this, true)
    screenshotCheckBox = findViewById(R.id.screenshotCheckBox)
    logsCheckBox = findViewById(R.id.logsCheckBox)
    addAttachmentButton = findViewById(R.id.addAttachmentButton)
    additionalAttachments = findViewById(R.id.additionalAttachments)

    addAttachmentButton.setOnClickListener { AttachmentFactory.requestAttachment(activity) }
    additionalAttachments.adapter = attachmentsAdapter
    additionalAttachments.addItemDecoration(DividerItemDecoration(context, VERTICAL))
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    emitScreenshotChanges()
    emitLogsChanges()
    observeUiModels()
  }

  private fun emitScreenshotChanges() {
    screenshotCheckBox.checkChanges
        .mapNotNull { isChecked -> screenshot?.let { isChecked to it } }
        .onEach { (isChecked, screenshot) ->
          val state = createAttachState(isChecked, screenshot)
          presenter.sendEvent(UpdateInput.screenshot(state))
        }
        .launchIn(viewScope)
  }

  private fun emitLogsChanges() {
    logsCheckBox.checkChanges
        .mapNotNull { isChecked -> logs?.let { isChecked to it } }
        .onEach { (isChecked, logs) ->
          val state = createAttachState(isChecked, logs)
          presenter.sendEvent(UpdateInput.logs(state))
        }
        .launchIn(viewScope)
  }

  private fun observeUiModels() {
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    val input = uiModel.input
    screenshot = input.screenshotState.file
    logs = input.logsState.file
    attachmentsAdapter.submitList(input.attachments.toList())
    screenshotCheckBox.setState(input.screenshotState)
    logsCheckBox.setState(input.logsState)
  }

  private fun CheckBox.setState(state: AttachState) {
    if (isChecked && state is Attach) return
    if (!isChecked && state is DoNotAttach) return
    isChecked = state.shouldCheck
    isEnabled = state.shouldEnable
    paintFlags = state.applyPaintFlag(paintFlags)
  }

  private val AttachState.shouldCheck
    get() = when (this) {
      is Attach -> true
      is DoNotAttach, is Unavailable -> false
    }

  private val AttachState.shouldEnable
    get() = when (this) {
      is Attach, is DoNotAttach -> true
      is Unavailable -> false
    }

  private fun AttachState.applyPaintFlag(flags: Int): Int {
    return when (this) {
      is Attach, is DoNotAttach -> flags and STRIKE_THRU_TEXT_FLAG.inv()
      is Unavailable -> flags or STRIKE_THRU_TEXT_FLAG
    }
  }

  private fun createAttachState(isChecked: Boolean, file: File): AttachState {
    return if (isChecked) Attach(file) else DoNotAttach(file)
  }
}
