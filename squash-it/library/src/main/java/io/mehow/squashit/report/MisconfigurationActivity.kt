package io.mehow.squashit.report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import io.mehow.squashit.BaseActivity
import io.mehow.squashit.R
import io.mehow.squashit.report.ReportConfig.Invalid
import io.mehow.squashit.report.extensions.enableEdgeToEdgeAndNightMode
import kotlinx.android.parcel.Parcelize

internal class MisconfigurationActivity : BaseActivity() {
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    window.decorView.enableEdgeToEdgeAndNightMode()
    val config = intent.getParcelableExtra<Args>(ArgsKey)!!.config
    setContentView(R.layout.squash_it_misconfiguration)

    findViewById<View>(R.id.projectKeyLabel).isVisible = !config.hasProjectKey
    findViewById<View>(R.id.serverUrlLabel).isVisible = !config.hasJiraUrl
    findViewById<View>(R.id.userEmailLabel).isVisible = !config.hasUserEmail
    findViewById<View>(R.id.userTokenLabel).isVisible = !config.hasUserToken
    findViewById<View>(R.id.goBackButton).setOnClickListener { onBackPressed() }
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.warningLabel)) { view, insets ->
      view.updatePadding(top = insets.systemWindowInsetTop)
      return@setOnApplyWindowInsetsListener insets
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    overridePendingTransition(R.anim.no_op, R.anim.slide_down)
  }

  companion object {
    private const val ArgsKey = "MisconfigurationActivity.Args"

    fun start(activity: Activity, args: Args) {
      val start = Intent(activity, MisconfigurationActivity::class.java).putExtra(ArgsKey, args)
      activity.startActivity(start)
      activity.overridePendingTransition(R.anim.slide_up, R.anim.no_op)
    }
  }

  @Parcelize
  internal data class Args(
    val config: Invalid
  ) : Parcelable
}
