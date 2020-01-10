package io.mehow.squashit

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class SquashItApp : DaggerApplication() {
  override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
    return DaggerSquashItComponent.factory().create(this)
  }
}
