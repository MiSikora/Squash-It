package io.mehow.squashit

import android.app.Activity
import android.content.Context
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import io.mehow.squashit.databinding.MainBinding

class MainBinder(activity: Activity, callback: Callback) {
  private val binding = MainBinding.inflate(activity.layoutInflater)
  private val credentialsAdapter = CredentialsAdapter(activity.layoutInflater, callback::onDelete)
  private val promptController = PromptController(binding.root, callback::onUndoDelete)

  init {
    setUpContainer()
    setUpCredentials(activity)
    setUpSaveCard(callback::onUpsert)
    activity.setContentView(binding.root)
  }

  fun renderUi(model: UiModel) {
    credentialsAdapter.submitList(model.credentials)
    promptController.submitState(model.state)
  }

  private fun setUpContainer() {
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
      view.updatePadding(top = insets.systemWindowInsetTop)
      return@setOnApplyWindowInsetsListener insets
    }
  }

  private fun setUpCredentials(context: Context) {
    ViewCompat.setOnApplyWindowInsetsListener(binding.credentials) { view, insets ->
      view.updatePadding(bottom = insets.systemWindowInsetBottom)
      return@setOnApplyWindowInsetsListener insets
    }
    binding.credentials.adapter = credentialsAdapter
    binding.credentials.addItemDecoration(DividerItemDecoration(context, VERTICAL))
  }

  private fun setUpSaveCard(onSaveClick: (Credentials) -> Unit) {
    binding.confirm.setOnClickListener {
      val credentials = getCredentials() ?: return@setOnClickListener
      onSaveClick(credentials)
      if (credentials.id.value.isNotBlank()) hideKeyboard()
    }

    binding.secret.setOnEditorActionListener { _, actionId, _ ->
      val credentials = getCredentials() ?: return@setOnEditorActionListener false
      if (actionId == IME_ACTION_DONE) {
        onSaveClick(credentials)
        if (credentials.id.value.isBlank()) return@setOnEditorActionListener true
      }
      return@setOnEditorActionListener false
    }
  }

  private fun getCredentials(): Credentials? {
    val userId = binding.userId.text?.toString() ?: return null
    val secret = binding.secret.text?.toString() ?: return null
    return Credentials.Impl(CredentialsId(userId), Secret(secret))
  }

  private fun hideKeyboard() {
    val context = binding.root.context
    val windowToken = binding.root.windowToken
    val inputMethodManager = context.getSystemService<InputMethodManager>()!!
    inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
  }

  interface Callback {
    fun onUpsert(credentials: Credentials)
    fun onDelete(credentials: Credentials)
    fun onUndoDelete(credentials: Credentials)
  }
}
