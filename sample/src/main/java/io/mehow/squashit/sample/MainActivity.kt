package io.mehow.squashit.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val logInput = findViewById<EditText>(R.id.logInput)
    findViewById<View>(R.id.logButton).setOnClickListener {
      val log = logInput.text?.toString() ?: return@setOnClickListener
      Timber.d(log)
    }
    findViewById<View>(R.id.openSecondActivityButton).setOnClickListener {
      startActivity(Intent(this, SecondActivity::class.java))
    }
  }
}
