package io.mehow.squashit.sample

import android.app.Application
import io.mehow.squashit.SquashItConfigurator

class SampleApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    SquashItConfigurator
        .jiraUrl("JiraURl")
        .userEmail("UserEmail")
        .userToken("UserToken")
        .projectKey("ProjectKey")
        .configure(this)
  }
}
