package io.mehow.squashit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.Duration
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import io.mehow.squashit.databinding.MainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Provider

class MainActivity : AppCompatActivity() {
  @Inject lateinit var presenterFactory: Provider<Presenter>
  private lateinit var presenter: Presenter
  private lateinit var binding: MainBinding
  private var currentSnackbar: Snackbar? = null
  private val mainScope = MainScope()

  override fun onCreate(inState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(inState)
    presenter = lastNonConfigurationInstance as? Presenter
        ?: presenterFactory.get().also { it.start() }
    binding = MainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    presenter.uiModels
        .onEach { renderUiModel(it) }
        .launchIn(mainScope)
  }

  private fun renderUiModel(model: UiModel) {
    Log.d("SquashIt", "$model")
  }

  override fun onRetainCustomNonConfigurationInstance(): Any? = presenter

  override fun onDestroy() {
    super.onDestroy()
    mainScope.cancel("Main Activity destroyed.")
    if (!isChangingConfigurations) presenter.stop()
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
