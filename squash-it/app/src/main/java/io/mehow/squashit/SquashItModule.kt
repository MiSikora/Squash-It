package io.mehow.squashit

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.mehow.squashit.SquashItModule.Contributors
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.time.seconds

@Module(includes = [Contributors::class])
object SquashItModule {
  private const val DbName = "squash-it.db"

  @Provides fun context(app: SquashItApp): Context = app

  @Provides @Singleton fun database(context: Context): Database {
    val driver = AndroidSqliteDriver(Database.Schema, context, DbName)
    return DatabaseFactory.create(driver)
  }

  @Provides @Io fun ioContext(): CoroutineContext = Dispatchers.IO

  @Provides @Presentation fun presentationContext(): CoroutineContext = Dispatchers.Unconfined

  @Provides fun promptDuration(): Duration = Duration(3.5.seconds)

  @Provides fun moshi(): Moshi {
    return Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }

  @Module
  abstract class Contributors {
    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun credentialsContentProvider(): CredentialsContentProvider

    @ContributesAndroidInjector
    abstract fun exportService(): ExportService

    @ContributesAndroidInjector
    abstract fun importService(): ImportService
  }
}
