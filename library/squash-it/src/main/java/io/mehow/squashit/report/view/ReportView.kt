package io.mehow.squashit.report.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.mehow.squashit.R
import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.ReportAdapter
import io.mehow.squashit.report.extensions.layoutInflater

internal class ReportView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
  private lateinit var toolbar: Toolbar
  private lateinit var viewPager: ViewPager2
  private lateinit var tabLayout: TabLayout

  init {
    orientation = VERTICAL
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    toolbar = findViewById(R.id.toolbar)
    viewPager = findViewById(R.id.viewPager)
    tabLayout = findViewById(R.id.tabLayout)
    setUpTabs()

    val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
    ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, insets ->
      view.updatePadding(top = insets.systemWindowInsetTop)
      return@setOnApplyWindowInsetsListener insets
    }
  }

  private fun setUpTabs() {
    toolbar.title = resources.getString(
        R.string.squash_it_report_an_issue,
        SquashItConfig.Instance.projectKey
    )
    val reportAdapter = ReportAdapter(context.layoutInflater)
    viewPager.apply {
      adapter = reportAdapter
      offscreenPageLimit = 1
    }
    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
      tab.setText(reportAdapter.getTabTitle(position))
    }.attach()
  }
}
