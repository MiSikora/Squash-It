package io.mehow.squashit.extensions

import android.content.Context
import android.view.LayoutInflater

internal val Context.layoutInflater get() = LayoutInflater.from(this)
