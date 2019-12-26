package io.mehow.squashit.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

internal inline fun <reified T : View> LayoutInflater.doInflate(
  @LayoutRes resource: Int,
  parent: ViewGroup?,
  attachToRoot: Boolean = false
): T = inflate(resource, parent, attachToRoot) as T
