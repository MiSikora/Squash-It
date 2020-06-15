object Libs {
  const val AndroidGradlePlugin = "com.android.tools.build:gradle:4.0.0"

  object Kotlin {
    const val Version = "1.3.72"

    const val StdLibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$Version"

    const val GradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$Version"

    object Coroutines {
      const val Version = "1.3.7"

      const val Android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$Version"

      const val Test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$Version"
    }
  }

  object AndroidX {
    const val AppCompat = "androidx.appcompat:appcompat:1.1.0"

    const val ConstraintLayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta7"

    const val CoreKtx = "androidx.core:core-ktx:1.3.0"

    const val PaletteKtx = "androidx.palette:palette-ktx:1.0.0"
  }

  const val Material = "com.google.android.material:material:1.2.0-beta01"

  const val Okio = "com.squareup.okio:okio:2.4.1"

  const val OkHttp = "com.squareup.okhttp3:okhttp:4.7.2"

  object Moshi {
    const val Version = "1.9.3"

    const val Runtime = "com.squareup.moshi:moshi-kotlin:$Version"

    const val Compiler = "com.squareup.moshi:moshi-kotlin-codegen:$Version"
  }

  object Retrofit {
    const val Version = "2.9.0"

    const val Core = "com.squareup.retrofit2:retrofit:$Version"

    const val MoshiConverter = "com.squareup.retrofit2:converter-moshi:$Version"
  }

  const val Timber = "com.jakewharton.timber:timber:4.7.1"

  const val ByteUnits = "com.jakewharton.byteunits:byteunits:0.9.1"

  object Dagger {
    const val Version = "2.28"

    const val Runtime = "com.google.dagger:dagger:$Version"

    const val Compiler = "com.google.dagger:dagger-compiler:$Version"

    const val AndroidRuntime = "com.google.dagger:dagger-android:$Version"

    const val AndroidCompiler = "com.google.dagger:dagger-android-processor:$Version"
  }

  object SqlDelight {
    const val Version = "1.3.0"

    const val DriverJvm = "com.squareup.sqldelight:sqlite-driver:$Version"

    const val DriverAndroid = "com.squareup.sqldelight:android-driver:$Version"

    const val CoroutinesExtensions = "com.squareup.sqldelight:coroutines-extensions:$Version"

    const val GradlePlugin = "com.squareup.sqldelight:gradle-plugin:$Version"
  }

  const val JUnit = "junit:junit:4.13"

  const val KotlinTestAssertions = "io.kotlintest:kotlintest-assertions:3.4.2"

  object Detekt {
    const val Version = "1.8.0"

    const val GradlePluginId = "io.gitlab.arturbosch.detekt"

    const val GradlePlugin = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$Version"

    const val Formatting = "io.gitlab.arturbosch.detekt:detekt-formatting:$Version"

    const val Cli = "io.gitlab.arturbosch.detekt:detekt-cli:$Version"
  }

  object GradleVersions {
    const val Version = "0.28.0"

    const val GradlePluginId = "com.github.ben-manes.versions"

    const val GradlePlugin = "com.github.ben-manes:gradle-versions-plugin:$Version"
  }

  const val MavenPublishGradlePlugin = "com.vanniktech:gradle-maven-publish-plugin:0.11.1"
}
