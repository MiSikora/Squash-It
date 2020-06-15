plugins {
  id("com.android.library")
  kotlin("android")
}

dependencies {
  api(Libs.Timber)
  api(project(":squash-it:library"))

  implementation(Libs.Kotlin.StdLibJdk7)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
