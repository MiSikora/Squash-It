package io.mehow.squashit

import android.app.Activity
import android.content.Context
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
      val userId = binding.userId.text?.toString() ?: return@setOnClickListener
      val secret = binding.secret.text?.toString() ?: return@setOnClickListener
      val credentials = Credentials.Impl(CredentialsId(userId), Token(secret))
      onSaveClick(credentials)
    }
  }

  interface Callback {
    fun onUpsert(credentials: Credentials)
    fun onDelete(credentials: Credentials)
    fun onUndoDelete(credentials: Credentials)
  }
}
