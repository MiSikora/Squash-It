package io.mehow.squashit.external

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ExternalModule {
  @ContributesAndroidInjector abstract fun credentialsContentProvider(): CredentialsContentProvider
}
