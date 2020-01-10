package io.mehow.squashit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import io.mehow.squashit.databinding.MainBinding

class MainActivity : AppCompatActivity() {
  override fun onCreate(inState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(inState)
    val binding = MainBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}
