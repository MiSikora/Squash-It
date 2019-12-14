package io.mehow.squashit.timber

import io.mehow.squashit.SquashItLogger
import timber.log.Timber.Tree

internal object SquashItTimberTree : Tree() {
  override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
    SquashItLogger.log(priority, tag ?: "SquashIt", message)
  }
}
