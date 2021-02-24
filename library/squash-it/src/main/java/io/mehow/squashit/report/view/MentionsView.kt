package io.mehow.squashit.report.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.mehow.squashit.R
import io.mehow.squashit.report.Mentions
import io.mehow.squashit.report.User
import io.mehow.squashit.report.extensions.doInflate
import io.mehow.squashit.report.extensions.layoutInflater
import io.mehow.squashit.report.extensions.viewScope
import io.mehow.squashit.report.presentation.Event.UpdateInput
import io.mehow.squashit.report.presentation.ReportPresenter
import io.mehow.squashit.report.presentation.UiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor") // Created with a custom factory.
internal class MentionsView(
  context: Context,
  attrs: AttributeSet?,
  private val presenter: ReportPresenter,
) : LinearLayout(context, attrs) {
  private val mentionsChipGroup: ChipGroup
  private val currentNameHandles = mutableSetOf<String>()

  init {
    orientation = VERTICAL

    LayoutInflater.from(context).inflate(R.layout.squash_it_mentions, this, true)
    mentionsChipGroup = findViewById(R.id.mentionsChipGroup)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(viewScope)
  }

  private fun renderUiModel(uiModel: UiModel) {
    val input = uiModel.input
    createMentionGroup(uiModel.projectInfo?.users.orEmpty(), input.mentions)
  }

  private fun createMentionGroup(users: Set<User>, mentions: Mentions) {
    isVisible = users.isNotEmpty()

    val inflater = context.layoutInflater
    val newUsers = users.filterNot { it.nameHandle in currentNameHandles }
    for (user in newUsers.sortedBy { it.nameHandle }) {
      val chip = inflater.doInflate<Chip>(R.layout.squash_it_user_chip, mentionsChipGroup)
      chip.text = user.nameHandle
      chip.isChecked = user in mentions.users
      chip.setOnCheckedChangeListener { _, isChecked ->
        viewScope.launch {
          val event = if (isChecked) UpdateInput.mention(user) else UpdateInput.unmention(user)
          presenter.sendEvent(event)
        }
      }
      mentionsChipGroup.addView(chip)
      currentNameHandles.add(user.nameHandle)
    }
  }
}
