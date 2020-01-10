package io.mehow.squashit

import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector

class SquashItApp : Application(), HasAndroidInjector {
  private lateinit var component: SquashItComponent

  override fun onCreate() {
    super.onCreate()
    component = DaggerSquashItComponent.factory().create(this)
  }

  override fun androidInjector(): AndroidInjector<Any> {
    return component.androidInjector
  }
}
