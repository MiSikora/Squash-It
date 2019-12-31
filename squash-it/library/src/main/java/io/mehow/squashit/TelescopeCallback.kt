package io.mehow.squashit

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.content.ContextCompat
import com.mattprecious.telescope.TelescopeLayout
import io.mehow.squashit.report.ReportLens

internal object TelescopeCallback : ActivityLifecycleCallbacks {
  override fun onActivityCreated(activity: Activity, inState: Bundle?) {
    if (activity is BaseActivity) return
    val content = activity.findViewById<View>(android.R.id.content)
    content.replug(createTelescopeLayout(activity))
  }

  private fun View.replug(newParent: ViewGroup) {
    val viewParent = parent as ViewGroup
    val viewGrandParent = viewParent.parent as ViewGroup
    val index = viewGrandParent.indexOfChild(viewParent)
    viewGrandParent.removeView(viewParent)
    newParent.addView(viewParent)
    viewGrandParent.addView(newParent, index)
  }

  private fun createTelescopeLayout(activity: Activity): TelescopeLayout {
    return TelescopeLayout(activity).apply {
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
      setLens(ReportLens(activity))
      setPointerCount(activity.resources.getInteger(R.integer.squash_it_report_pointer_count))
      setProgressColor(ContextCompat.getColor(activity, R.color.squash_it_report_progress_color))
    }
  }

  override fun onActivityStarted(activity: Activity) = Unit
  override fun onActivityResumed(activity: Activity) = Unit
  override fun onActivityPaused(activity: Activity) = Unit
  override fun onActivityStopped(activity: Activity) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
  override fun onActivityDestroyed(activity: Activity) = Unit
}
