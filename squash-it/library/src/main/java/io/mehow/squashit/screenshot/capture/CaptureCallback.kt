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

  override fun onActivityStarted(activity: Activity) {
    val trigger = screenshotTriggers[activity]
    if (trigger != null) {
      println("LOG_TAG: START")
      TriggerScreenshotService.start(activity)
      trigger.register(activity)
    }
  }

  override fun onActivityStopped(activity: Activity) {
    val trigger = screenshotTriggers[activity]
    if (trigger != null) {
      println("LOG_TAG: STOP")
      trigger.unregister(activity)
      TriggerScreenshotService.stop(activity)
    }
  }

  override fun onActivityResumed(activity: Activity) = Unit
  override fun onActivityPaused(activity: Activity) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
