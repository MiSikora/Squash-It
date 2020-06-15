import java.util.Properties

plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
  id("com.squareup.sqldelight")
}


val gitSha = gitSha()
base.archivesBaseName  = "squash-it-$gitSha"

android {
  defaultConfig {
    applicationId  ="io.mehow.squashit"

    versionCode = headCommitCount()
    versionName = "0.3.2"
    versionNameSuffix = "-$gitSha"

    proguardFiles("shrink-rules.pro")
  }

  buildFeatures {
    viewBinding = true
  }

  val uploadKeystore = file("upload.keystore")
  val keystoreProperties = file("upload-keystore.properties")
  val useUploadKeystore = uploadKeystore.exists() && keystoreProperties.exists()

  signingConfigs {
    named("debug") {
      keyAlias = "makani"
      keyPassword = "makani"
      storeFile = file("debug.keystore")
      storePassword = "makani"
    }
    if (useUploadKeystore) {
      val signingProps = Properties().apply {
        load(keystoreProperties.inputStream())
      }
      register("upload") {
        keyAlias = "makani"
        keyPassword = signingProps["keyPassword"].toString()
        storeFile = uploadKeystore
        storePassword = signingProps["storePassword"].toString()
      }
    }
  }

  buildTypes {
    named("debug") {
      signingConfig  = signingConfigs.getByName("debug")
    }

    named("release") {
      if (useUploadKeystore) {
        signingConfig = signingConfigs.getByName("upload")
      } else {
        signingConfig = signingConfigs.getByName("debug")
      }

      isMinifyEnabled = true
      isShrinkResources = true
    }
  }

  packagingOptions {
    exclude("**/*.kotlin_metadata")
    exclude("**/*.properties")
    exclude("*.properties")
    exclude("kotlin/**")
    exclude("META-INF/*.version")
    exclude("META-INF/proguard/*")
    exclude("META-INF/*.properties")
    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/androidx.*")
    exclude("META-INF/CHANGES")
    exclude("META-INF/CHANGES.md")
    exclude("META-INF/CHANGES.txt")
    exclude("META-INF/README")
    exclude("META-INF/README.md")
    exclude("META-INF/README.txt")
  }
}

sqldelight {
  database("Database") {
    packageName = "io.mehow.squashit"
  }
}

dependencies {
  implementation(Libs.Kotlin.StdLibJdk7)
  implementation(Libs.AndroidX.AppCompat)
  implementation(Libs.AndroidX.CoreKtx)
  implementation(Libs.AndroidX.ConstraintLayout)
  implementation(Libs.Material)
  implementation(Libs.Dagger.Runtime)
  kapt(Libs.Dagger.Compiler)
  implementation(Libs.Dagger.AndroidRuntime)
  kapt(Libs.Dagger.AndroidCompiler)
  implementation(Libs.Kotlin.Coroutines.Android)
  implementation(Libs.SqlDelight.DriverAndroid)
  implementation(Libs.SqlDelight.CoroutinesExtensions)
  implementation(Libs.Okio)
  implementation(Libs.Moshi.Runtime)
  kapt(Libs.Moshi.Compiler)

  testImplementation(Libs.JUnit)
  testImplementation(Libs.KotlinTestAssertions)
  testImplementation(Libs.Kotlin.Coroutines.Test)
  testImplementation(Libs.SqlDelight.DriverJvm)
}

fun gitSha(): String {
  return "git rev-parse HEAD".execute(project.rootDir)
}

fun headCommitCount(): Int {
  return "git rev-list --count HEAD".execute(project.rootDir).toInt()
}

fun String.execute(dir: File): String {
  val process = Runtime.getRuntime().exec(this, null, dir)
  process.waitFor()
  return process.inputStream.reader().readText().trim()
}
