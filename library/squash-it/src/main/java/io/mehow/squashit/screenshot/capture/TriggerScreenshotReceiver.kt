package io.mehow.squashit.screenshot.capture

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper

internal class TriggerScreenshotReceiver(
  private val screenshotProvider: ScreenshotProvider
) : BroadcastReceiver() {
  private var isRegistered = false

  fun register(activity: Activity) {
    isRegistered = true
    activity.registerReceiver(this, IntentFilter(BroadcastAction))
  }

  fun unregister(activity: Activity) {
    isRegistered = false
    activity.unregisterReceiver(this)
  }

  fun stopTrigger() {
    screenshotProvider.stopBackgroundThread()
  }

  override fun onReceive(context: Context, intent: Intent) {
    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    // Add small delay to account for closing system dialogs
    Handler(Looper.getMainLooper()).postDelayed({
      if (isRegistered) screenshotProvider.takeScreenshot()
    }, 400L)
  }

  companion object {
    private const val BroadcastAction = "io.mehow.squashit.screenshot.trigger.action"

    fun intent() = Intent(BroadcastAction)
  }
}
