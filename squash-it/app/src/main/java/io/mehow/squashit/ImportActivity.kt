package io.mehow.squashit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.content.Intent.CATEGORY_OPENABLE
import android.os.Bundle

class ImportActivity : Activity() {
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    requestCredentials()
  }

  @Suppress("ComplexCondition")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val uri = data?.data
    if (resultCode == RESULT_OK && requestCode == CredentialsRequestCode && uri != null) {
      ImportService.start(this, uri)
    }
    finish()
  }

  private fun requestCredentials() {
    val intent = Intent(ACTION_OPEN_DOCUMENT).apply {
      addCategory(CATEGORY_OPENABLE)
      type = "application/json"
    }
    startActivityForResult(intent, CredentialsRequestCode)
  }

  companion object {
    private const val CredentialsRequestCode = 200

    fun start(context: Context) {
      val intent = Intent(context, ImportActivity::class.java)
      context.startActivity(intent)
    }
  }
}
