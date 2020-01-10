package io.mehow.squashit

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
      AndroidInjectionModule::class,
      MainActivityModule::class,
      SquashItModule::class
    ]
)
internal interface SquashItComponent : AndroidInjector<SquashItApp> {
  @Component.Factory
  interface Factory : AndroidInjector.Factory<SquashItApp>
}
