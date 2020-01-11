package io.mehow.squashit.sample

import android.app.Application
import io.mehow.squashit.Credentials
import io.mehow.squashit.SquashItConfigurator

class SampleApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    SquashItConfigurator
        .projectKey("ProjectKey")
        .jiraUrl("JiraUrl")
        .credentialsProvider { Credentials("UserId", "Secret") }
        .configure(this)
  }
}
