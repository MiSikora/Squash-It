package io.mehow.squashit

import android.app.Activity
import com.mattprecious.telescope.Lens
import java.io.File

internal class ReportLens(
  private val activity: Activity
) : Lens() {
  override fun onCapture(screenshot: File?) {
    SquashItConfig
        .create(activity)
        .startActivity(activity, screenshot)
  }
}
