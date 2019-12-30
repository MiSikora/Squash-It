package io.mehow.squashit

import android.app.Activity
import com.mattprecious.telescope.Lens
import io.mehow.screenshot.ScreenshotActivity
import io.mehow.screenshot.ScreenshotActivity.Args
import java.io.File

internal class ReportLens(
  private val activity: Activity
) : Lens() {
  override fun onCapture(screenshot: File?) {
    if (screenshot == null) SquashItConfig.create(activity).startActivity(activity, screenshot)
    else ScreenshotActivity.start(activity, Args(screenshot))
  }
}
