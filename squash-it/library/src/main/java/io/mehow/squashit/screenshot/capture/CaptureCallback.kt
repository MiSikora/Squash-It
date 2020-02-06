package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import io.mehow.squashit.NoScreenshots
import java.io.File

internal class CaptureCallback(
  private val onScreenshot: (Activity, File?) -> Unit
) : ActivityLifecycleCallbacks {
  private val detectors = mutableMapOf<Activity, Pair<CaptureDetector, ScreenshotProvider>>()

  override fun onActivityCreated(activity: Activity, inState: Bundle?) {
    if (activity is NoScreenshots) return
    val screenshotProvider = ScreenshotProvider(activity) { onScreenshot(activity, it) }
    detectors[activity] = CaptureDetector.create(activity) {
      screenshotProvider.takeScreenshot()
    } to screenshotProvider
  }

  override fun onActivityDestroyed(activity: Activity) {
    detectors.remove(activity)?.second?.stopBackgroundThread()
  }

  override fun onActivityStarted(activity: Activity) {
    detectors[activity]?.first?.attach(activity)
  }

  override fun onActivityStopped(activity: Activity) {
    detectors[activity]?.first?.detach(activity)
  }

  override fun onActivityResumed(activity: Activity) = Unit
  override fun onActivityPaused(activity: Activity) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
