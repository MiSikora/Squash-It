package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import io.mehow.squashit.NoScreenshots
import java.io.File

internal class CaptureCallback(
  private val onScreenshot: (Activity, File?) -> Unit
) : ActivityLifecycleCallbacks {
  private val screenshotTriggers = mutableMapOf<Activity, TriggerScreenshotReceiver>()

  override fun onActivityCreated(activity: Activity, inState: Bundle?) {
    if (activity is NoScreenshots) return
    val provider = ScreenshotProvider(activity) { onScreenshot(activity, it) }
    screenshotTriggers[activity] = TriggerScreenshotReceiver(provider)
  }

  override fun onActivityDestroyed(activity: Activity) {
    screenshotTriggers.remove(activity)?.stopTrigger()
  }

  override fun onActivityResumed(activity: Activity) {
    val trigger = screenshotTriggers[activity]
    if (trigger != null) {
      TriggerScreenshotService.start(activity)
      trigger.register(activity)
    }
  }

  override fun onActivityPaused(activity: Activity) {
    val trigger = screenshotTriggers[activity]
    if (trigger != null) {
      trigger.unregister(activity)
      TriggerScreenshotService.stop(activity)
    }
  }

  override fun onActivityStarted(activity: Activity) = Unit
  override fun onActivityStopped(activity: Activity) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
