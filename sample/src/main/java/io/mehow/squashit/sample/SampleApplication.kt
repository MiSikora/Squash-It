package io.mehow.squashit.sample

import android.app.Application
import io.mehow.squashit.CredentialsProvider
import io.mehow.squashit.SquashItConfigurator

class SampleApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    SquashItConfigurator
        .projectKey("MSS")
        .jiraUrl("https://droidsonroids.atlassian.net")
        .credentialsProvider(CredentialsProvider("makanibot@droidsonroids.pl"))
        .configure(this)
  }
}
