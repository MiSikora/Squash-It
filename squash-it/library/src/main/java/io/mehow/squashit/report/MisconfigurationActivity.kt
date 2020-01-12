package io.mehow.squashit.report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import io.mehow.squashit.NoTelescope
import io.mehow.squashit.R
import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.extensions.enableEdgeToEdgeAndNightMode

internal class MisconfigurationActivity : Activity(), NoTelescope {
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    window.decorView.enableEdgeToEdgeAndNightMode()
    setContentView(R.layout.squash_it_misconfiguration)

    findViewById<View>(R.id.projectKeyLabel).isVisible = !SquashItConfig.Instance.hasProjectKey
    findViewById<View>(R.id.serverUrlLabel).isVisible = !SquashItConfig.Instance.hasJiraUrl
    findViewById<View>(R.id.userEmailLabel).isVisible = !SquashItConfig.Instance.hasUserEmail
    findViewById<View>(R.id.userTokenLabel).isVisible = !SquashItConfig.Instance.hasUserToken
    findViewById<View>(R.id.goBack).setOnClickListener { onBackPressed() }
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
    fun start(activity: Activity) {
      val start = Intent(activity, MisconfigurationActivity::class.java)
      activity.startActivity(start)
      activity.overridePendingTransition(R.anim.slide_up, R.anim.no_op)
    }
  }
}
