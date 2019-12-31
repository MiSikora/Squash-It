package io.mehow.squashit.report

import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import io.mehow.squashit.R.layout
import io.mehow.squashit.R.string
import io.mehow.squashit.report.view.StatefulRecyclerAdapter

internal class ReportAdapter(
  inflater: LayoutInflater
) : StatefulRecyclerAdapter(inflater, 2) {
  private val screenIds = listOf(
      string.squash_it_issue_tab_title to layout.issue_page,
      string.squash_it_add_ons_tab_title to layout.add_ons_page
  )

  @StringRes fun getTabTitle(position: Int) = screenIds[position].first

  @LayoutRes override fun getItemViewType(position: Int) = screenIds[position].second

  override fun getItemCount() = screenIds.size
}
