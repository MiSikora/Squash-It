package io.mehow.squashit

import android.app.Activity
import android.os.Bundle

class ExportActivity : Activity() {
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    ExportService.start(this)
    finish()
  }
}
