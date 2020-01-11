package io.mehow.squashit.report

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import io.mehow.squashit.BaseActivity
import io.mehow.squashit.R
import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.report.extensions.enableEdgeToEdgeAndNightMode

internal class MisconfigurationActivity : BaseActivity() {
  @Suppress("LongMethod")
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    window.decorView.enableEdgeToEdgeAndNightMode()
    setContentView(R.layout.squash_it_misconfiguration)

    with(SquashItConfig.Instance) {
      val hasCredentialsData = hasUserId && hasUserSecret
      findViewById<View>(R.id.projectKeyLabel).isVisible = !hasProjectKey
      findViewById<View>(R.id.serverUrlLabel).isVisible = !hasJiraUrl
      findViewById<View>(R.id.credentialsLabel).isVisible = !hasCredentialsData

      val credentialsButton = findViewById<Button>(R.id.handleCredentials)
      credentialsButton.isVisible = !hasCredentialsData
      val hasSquashIt = isSquashItInstalled
      val text = if (hasSquashIt) R.string.squash_it_open_squash_it
      else R.string.squash_it_get_squash_it
      credentialsButton.setText(text)
      credentialsButton.setOnClickListener {
        if (hasSquashIt) openSquashIt() else showSquashItInPlayStore()
      }
    }

    findViewById<View>(R.id.goBack).setOnClickListener { onBackPressed() }
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.warningLabel)) { view, insets ->
      view.updatePadding(top = insets.systemWindowInsetTop)
      return@setOnApplyWindowInsetsListener insets
    }
  }

  private val isSquashItInstalled: Boolean
    get() {
      return runCatching { packageManager.getApplicationInfo("io.mehow.squashit", 0) }
          .map { true }
          .getOrDefault(false)
    }

  override fun onBackPressed() {
    super.onBackPressed()
    overridePendingTransition(R.anim.no_op, R.anim.slide_down)
  }

  private fun openSquashIt() {
    startActivity(packageManager.getLaunchIntentForPackage("io.mehow.squashit"))
  }

  private fun showSquashItInPlayStore() {
    try {
      startActivity(playStoreAppIntent)
    } catch (_: Throwable) {
      startActivity(playStoreWebIntent)
    }
  }

  companion object {
    fun start(activity: Activity) {
      val start = Intent(activity, MisconfigurationActivity::class.java)
      activity.startActivity(start)
      activity.overridePendingTransition(R.anim.slide_up, R.anim.no_op)
    }

    private val playStoreAppIntent = Intent(
        ACTION_VIEW,
        "market://details?id=io.mehow.squashit".toUri()
    )
    private val playStoreWebIntent = Intent(
        ACTION_VIEW,
        "https://play.google.com/store/apps/details?id=io.mehow.squashit".toUri()
    )
  }
}
