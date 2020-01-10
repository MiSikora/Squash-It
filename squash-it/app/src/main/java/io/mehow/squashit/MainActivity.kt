package io.mehow.squashit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.Duration
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import io.mehow.squashit.databinding.MainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: MainBinding
  private var currentSnackbar: Snackbar? = null

  override fun onCreate(inState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(inState)
    binding = MainBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }

  fun showPrompt(
    message: String,
    @Duration duration: Int = LENGTH_INDEFINITE,
    builder: Snackbar.() -> Unit = {}
  ) {
    hidePrompt()
    currentSnackbar = Snackbar.make(binding.root, message, duration).also { snackbar ->
      snackbar.builder()
      snackbar.show()
    }
  }

  fun hidePrompt() = currentSnackbar?.dismiss()
}
