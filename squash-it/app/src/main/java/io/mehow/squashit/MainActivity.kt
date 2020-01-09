package io.mehow.squashit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.mehow.squashit.databinding.MainBinding

class MainActivity : AppCompatActivity() {
  override fun onCreate(inState: Bundle?) {
    super.onCreate(inState)
    val binding = MainBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}
