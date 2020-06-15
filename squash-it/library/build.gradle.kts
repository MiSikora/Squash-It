plugins {
  id("com.android.library")
  kotlin("android")
  kotlin("kapt")
  id("kotlin-android-extensions")
}

dependencies {
  implementation(Libs.Kotlin.StdLibJdk7)
  implementation(Libs.Kotlin.Coroutines.Android)

  implementation(Libs.Okio)
  implementation(Libs.OkHttp)
  implementation(Libs.Moshi.Runtime)
  kapt(Libs.Moshi.Compiler)
  implementation(Libs.Retrofit.Core)
  implementation(Libs.Retrofit.MoshiConverter)

  implementation(Libs.AndroidX.AppCompat)
  implementation(Libs.AndroidX.ConstraintLayout)
  implementation(Libs.AndroidX.CoreKtx)
  implementation(Libs.AndroidX.PaletteKtx)
  implementation(Libs.Material)
  implementation(Libs.ByteUnits)

  testImplementation(Libs.JUnit)
  testImplementation(Libs.KotlinTestAssertions)
  testImplementation(Libs.Kotlin.Coroutines.Test)
}

apply(from = rootProject.file("gradle/gradle-mvn-push.gradle"))
