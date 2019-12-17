package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import io.mehow.squashit.R
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.Event.SyncProject
import io.mehow.squashit.extensions.clicks
import io.mehow.squashit.extensions.viewScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class InitFailureView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : LinearLayout(context, attrs) {
  private lateinit var reInitButton: Button

  init {
    orientation = VERTICAL
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    reInitButton = findViewById(R.id.retryInitializationButton)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    reInitButton.clicks
        .onEach { presenter.sendEvent(SyncProject) }
        .launchIn(viewScope)
  }
}
