package io.mehow.squashit

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {
  @ContributesAndroidInjector abstract fun mainActivity(): MainActivity
}
