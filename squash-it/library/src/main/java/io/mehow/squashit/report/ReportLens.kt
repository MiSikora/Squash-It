package io.mehow.squashit.report

import android.app.Activity
import com.mattprecious.telescope.Lens
import io.mehow.squashit.SquashItConfig
import io.mehow.squashit.screenshot.ScreenshotActivity
import io.mehow.squashit.screenshot.ScreenshotActivity.Args
import java.io.File

internal class ReportLens(private val activity: Activity) : Lens() {
  override fun onCapture(screenshot: File?) {
    if (screenshot == null) SquashItConfig.Instance.start(activity, screenshot)
    else ScreenshotActivity.start(activity, Args(screenshot))
  }
}
