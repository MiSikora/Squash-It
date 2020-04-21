package io.mehow.squashit

import android.app.Activity
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import dagger.android.AndroidInjection
import io.mehow.squashit.Event.DeleteCredentials
import io.mehow.squashit.Event.DismissPrompt
import io.mehow.squashit.Event.UpsertCredentials
import io.mehow.squashit.MainBinder.Callback
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class MainActivity : Activity() {
  @Inject lateinit var presenterFactory: Provider<Presenter>
  lateinit var presenter: Presenter
  val mainScope = MainScope()
  private val binderCallback = object : Callback {
    override fun onUpsert(credentials: Credentials) {
      mainScope.launch {
        presenter.sendEvent(UpsertCredentials(credentials))
      }
    }

    override fun onDelete(credentials: Credentials) {
      mainScope.launch {
        presenter.sendEvent(DeleteCredentials(credentials.id))
      }
    }

    override fun onUndoDelete(credentials: Credentials) {
      mainScope.launch {
        presenter.sendEvent(DismissPrompt)
        presenter.sendEvent(UpsertCredentials(credentials, showPrompt = false))
      }
    }

    override fun onExportCredentials() {
      ExportService.start(this@MainActivity)
    }

    override fun onImportCredentials() {
      ImportActivity.start(this@MainActivity)
    }
  }

  override fun onCreate(inState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(inState)
    presenter = lastNonConfigurationInstance
    window.decorView.enableEdgeToEdgeAndNightMode()
    val binder = MainBinder(this, binderCallback)
    presenter.uiModels
      .onEach { binder.renderUi(it) }
      .launchIn(mainScope)
  }

  override fun getLastNonConfigurationInstance(): Presenter {
    return super.getLastNonConfigurationInstance() as? Presenter
      ?: presenterFactory.get().also { it.start() }
  }

  override fun onRetainNonConfigurationInstance(): Presenter = presenter

  override fun onDestroy() {
    super.onDestroy()
    mainScope.cancel("Main Activity destroyed.")
    if (!isChangingConfigurations) presenter.stop()
  }

  private fun View.enableEdgeToEdgeAndNightMode() {
    val isDarkMode = resources.configuration.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    val flags = SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        SYSTEM_UI_FLAG_LAYOUT_STABLE or
        if (Build.VERSION.SDK_INT == 26 && isDarkMode) SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
    systemUiVisibility = systemUiVisibility or flags
  }
}
