import com.android.build.api.variant.VariantFilter

plugins {
  id("com.android.application")
  kotlin("android")
}

android {
  signingConfigs {
    register("config") {
      keyAlias = "mehow-io"
      keyPassword = "mehow-io"
      storeFile = file("mehow-io.keystore")
      storePassword = "mehow-io"
    }
  }

  defaultConfig {
    applicationId = "io.mehow.squashit.sample"

    versionCode = 1
    versionName = "1.0.0"

    signingConfig = signingConfigs.getByName("config")
  }

  buildTypes {
    named("debug") {
      setMatchingFallbacks("release")
    }
  }

  variantFilter = Action<VariantFilter> {
    ignore = name != "debug"
  }
}

dependencies {
  implementation(project(":squash-it:timber"))
  implementation(Libs.Kotlin.StdLibJdk7)
  implementation(Libs.AndroidX.AppCompat)
}
