@file:Suppress("TopLevelPropertyNaming")
package io.mehow.squashit.report.extensions

import android.app.Activity
import android.content.ContextWrapper
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import io.mehow.squashit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal val View.activity: Activity
  get() {
    var wrappedContext = context
    while (wrappedContext is ContextWrapper) {
      if (wrappedContext is Activity) return wrappedContext
      wrappedContext = wrappedContext.baseContext
    }
    error("No Activity was found for View.")
  }

internal fun View.enableEdgeToEdgeAndNightMode() {
  val isDarkMode = resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
  val flags = SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
      SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
      SYSTEM_UI_FLAG_LAYOUT_STABLE or
      if (Build.VERSION.SDK_INT == 26 && isDarkMode) SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
  systemUiVisibility = systemUiVisibility or flags
}

internal fun Button.showProgress(@StringRes text: Int) {
  setText(text)
  if (compoundDrawables.all { it == null }) {
    val drawable = AnimatedVectorDrawableCompat.create(context, R.drawable.squash_it_ic_progress)
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    drawable?.start()
  }
}

internal fun Button.hideProgress(@StringRes text: Int) {
  setText(text)
  setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}

internal val TextView.textChanges: Flow<String>
  get() = callbackFlow {
    val listener = object : TextWatcher {
      override fun afterTextChanged(editable: Editable?) {
        editable?.let { offer("$it") }
      }

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
  }

internal val CompoundButton.checkChanges
  get() = callbackFlow {
    setOnCheckedChangeListener { _, isChecked -> offer(isChecked) }
    awaitClose { setOnCheckedChangeListener(null) }
  }

internal val RadioGroup.checkChanges
  get() = callbackFlow {
    setOnCheckedChangeListener { _, isChecked -> offer(isChecked) }
    awaitClose { setOnCheckedChangeListener(null) }
  }

internal val View.clicks
  get() = callbackFlow {
    setOnClickListener { offer(Unit) }
    awaitClose { setOnClickListener(null) }
  }

internal val View.focuses
  get() = callbackFlow {
    setOnFocusChangeListener { _, hasFocus -> if (hasFocus) offer(Unit) }
    awaitClose { setOnClickListener(null) }
  }

internal val View.viewScope: CoroutineScope
  get() {
    val storedScope = getTag(R.string.squash_it_view_coroutine_scope) as? CoroutineScope
    if (storedScope != null) return storedScope

    val newScope = ViewCoroutineScope()
    if (isAttachedToWindow) {
      addOnAttachStateChangeListener(newScope)
      setTag(R.string.squash_it_view_coroutine_scope, newScope)
    } else newScope.cancel()

    return newScope
  }

private class ViewCoroutineScope : CoroutineScope, View.OnAttachStateChangeListener {
  override val coroutineContext = SupervisorJob() + Dispatchers.Main

  override fun onViewAttachedToWindow(view: View) = Unit

  override fun onViewDetachedFromWindow(view: View) {
    coroutineContext.cancel()
    view.setTag(R.string.squash_it_view_coroutine_scope, null)
  }
}
