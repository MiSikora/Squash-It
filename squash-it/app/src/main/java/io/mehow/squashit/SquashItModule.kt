package io.mehow.squashit

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
object SquashItModule {
  @Provides @Singleton fun provideDatabase(context: Context): Database {
    val driver = AndroidSqliteDriver(Database.Schema, context, "squash-it.db")
    return DatabaseFactory.create(driver)
  }

  @Provides @Io fun provideIoContext(): CoroutineContext = Dispatchers.IO
}
