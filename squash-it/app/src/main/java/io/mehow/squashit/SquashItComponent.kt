package io.mehow.squashit

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.DispatchingAndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
      AndroidInjectionModule::class,
      MainActivityModule::class,
      SquashItModule::class
    ]
)
internal interface SquashItComponent {
  val androidInjector: DispatchingAndroidInjector<Any>

  @Component.Factory
  interface Factory {
    fun create(@BindsInstance context: Context): SquashItComponent
  }
}
