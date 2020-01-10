package io.mehow.squashit

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.DispatchingAndroidInjector

@Component(modules = [AndroidInjectionModule::class, MainActivityModule::class])
internal interface SquashItComponent {
  val androidInjector: DispatchingAndroidInjector<Any>

  @Component.Factory
  interface Factory {
    fun create(): SquashItComponent
  }
}
