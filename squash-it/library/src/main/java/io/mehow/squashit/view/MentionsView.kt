package io.mehow.squashit.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.mehow.squashit.Mentions
import io.mehow.squashit.R
import io.mehow.squashit.User
import io.mehow.squashit.extensions.doInflate
import io.mehow.squashit.extensions.layoutInflater
import io.mehow.squashit.extensions.viewScope
import io.mehow.squashit.presentation.Event.MentionUser
import io.mehow.squashit.presentation.Event.UnmentionUser
import io.mehow.squashit.presentation.ReportPresenter
import io.mehow.squashit.presentation.UiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class MentionsView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter
) : LinearLayout(context, attrs) {
  private val mentionsChipGroup: ChipGroup
  private val currentNameHandles = mutableSetOf<String>()

  init {
    orientation = VERTICAL

    LayoutInflater.from(context).inflate(R.layout.mentions, this, true)
    mentionsChipGroup = findViewById(R.id.mentionsChipGroup)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    createMentionGroup(uiModel.projectInfo?.users.orEmpty(), uiModel.mentions)
  }

  private fun createMentionGroup(users: Set<User>, mentions: Mentions) {
    isVisible = users.isNotEmpty()

    val inflater = context.layoutInflater
    val newUsers = users.filterNot { it.nameHandle in currentNameHandles }
    for (user in newUsers.sortedBy { it.nameHandle }) {
      val chip = inflater.doInflate<Chip>(R.layout.user_chip, mentionsChipGroup)
      chip.text = user.nameHandle
      chip.isChecked = user in mentions.users
      chip.setOnCheckedChangeListener { _, isChecked ->
        viewScope.launch {
          val event = if (isChecked) MentionUser(user) else UnmentionUser(user)
          presenter.sendEvent(event)
        }
      }
      mentionsChipGroup.addView(chip)
      currentNameHandles.add(user.nameHandle)
    }
  }
}
