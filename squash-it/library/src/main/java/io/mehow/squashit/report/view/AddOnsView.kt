package io.mehow.squashit.report.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import io.mehow.squashit.R

internal class AddOnsView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {
  init {
    overScrollMode = OVER_SCROLL_NEVER
    LayoutInflater.from(context).inflate(R.layout.squash_it_add_ons, this, true)
    setUpInsets(findViewById(R.id.addOnsContent))
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    requestApplyInsets()
  }

  private fun setUpInsets(content: View) {
    val bottomRootPadding = content.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
      view.updatePadding(bottom = bottomRootPadding + insets.systemWindowInsetBottom)
      return@setOnApplyWindowInsetsListener insets
    }
  }
}
