package io.mehow.squashit

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.BaseTransientBottomBar.Behavior
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import io.mehow.squashit.ActionState.Added
import io.mehow.squashit.ActionState.Deleted
import io.mehow.squashit.ActionState.Idle
import io.mehow.squashit.ActionState.Updated
import io.mehow.squashit.R.id

class PromptController(
  private val layout: CoordinatorLayout,
  private val onUndoDelete: (Credentials) -> Unit
) {
  private val resources = layout.context.resources
  private var currentSnackbar: Snackbar? = null

  fun submitState(state: ActionState) {
    val (message, action) = when (state) {
      is Idle -> ActionHandler(null)
      is Added -> handleAddition(state)
      is Updated -> handleUpdate(state)
      is Deleted -> handleUndo(state)
    }
    if (message == null) hidePrompt() else showPrompt(message, action)
  }

  private fun handleAddition(state: Added): ActionHandler {
    val userId = state.credentials.id.value
    return ActionHandler(resources.getString(R.string.credentials_added, userId))
  }

  private fun handleUpdate(state: Updated): ActionHandler {
    val userId = state.credentials.id.value
    return ActionHandler(resources.getString(R.string.credentials_updated, userId))
  }

  private fun handleUndo(state: Deleted): ActionHandler {
    val userId = state.credentials.id.value
    return ActionHandler(resources.getString(R.string.credentials_deleted, userId)) {
      setAction(R.string.undo) {}
      addCallback(object : BaseCallback<Snackbar>() {
        override fun onShown(snackbar: Snackbar) {
          snackbar.view.findViewById<View>(id.snackbar_action).setOnClickListener {
            onUndoDelete(state.credentials)
          }
        }
      })
    }
  }

  private fun showPrompt(message: String, builder: Snackbar.() -> Unit) {
    hidePrompt()
    currentSnackbar = Snackbar.make(layout, message, LENGTH_INDEFINITE).also { snackbar ->
      snackbar.builder()
      snackbar.behavior = NoSwipeBehavior
      snackbar.show()
    }
  }

  private fun hidePrompt() = currentSnackbar?.dismiss()

  private data class ActionHandler(val message: String?, val action: (Snackbar.() -> Unit) = {})

  private object NoSwipeBehavior : Behavior() {
    override fun canSwipeDismissView(child: View) = false
  }
}
