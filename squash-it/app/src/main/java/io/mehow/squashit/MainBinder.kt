package io.mehow.squashit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.Gravity.NO_GRAVITY
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.PopupMenu
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
    setUpImportExport(callback::onExportCredentials)
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

  @SuppressLint("ClickableViewAccessibility") // Deliberate
  private fun setUpImportExport(onExport: () -> Unit) {
    val popup = createPopupMenu()
    popup.menuInflater.inflate(R.menu.import_export, popup.menu)
    popup.setOnMenuItemClickListener { menuItem ->
      if (menuItem.itemId == R.id.export) onExport()
      else error("Unknown menu item: $menuItem")
      return@setOnMenuItemClickListener true
    }
    binding.actions.setOnTouchListener(popup.dragToOpenListener)
    binding.actions.setOnClickListener { popup.show() }
  }

  private fun createPopupMenu(): PopupMenu {
    return PopupMenu(
        binding.root.context,
        binding.actions,
        NO_GRAVITY,
        0,
        android.R.style.Widget_Material_PopupMenu_Overflow
    )
  }

  interface Callback {
    fun onUpsert(credentials: Credentials)
    fun onDelete(credentials: Credentials)
    fun onUndoDelete(credentials: Credentials)
    fun onExportCredentials()
  }
}
