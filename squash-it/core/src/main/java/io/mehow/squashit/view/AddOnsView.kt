package io.mehow.squashit.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import io.mehow.squashit.R

internal class AddOnsView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {
  private val content: ConstraintLayout
  private val epicView: EpicView
  private val mentionsView: MentionsView
  private val attachmentsView: AttachmentsView

  init {
    overScrollMode = OVER_SCROLL_NEVER

    LayoutInflater.from(context).inflate(R.layout.add_ons, this, true)
    content = findViewById(R.id.addOnsContent)
    epicView = findViewById(R.id.epic)
    mentionsView = findViewById(R.id.mentions)
    attachmentsView = findViewById(R.id.attachments)

    setUpInsets()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    requestApplyInsets()
  }

  private fun setUpInsets() {
    val bottomRootPadding = content.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
      view.updatePadding(bottom = bottomRootPadding + insets.systemWindowInsetBottom)
      return@setOnApplyWindowInsetsListener insets
    }
  }
}
