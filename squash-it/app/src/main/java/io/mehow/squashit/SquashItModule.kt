package io.mehow.squashit

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.time.seconds

@Module
object SquashItModule {
  @Provides @Singleton fun database(context: Context): Database {
    val driver = AndroidSqliteDriver(Database.Schema, context, "squash-it.db")
    return DatabaseFactory.create(driver)
  }

  @Provides @Io fun ioContext(): CoroutineContext = Dispatchers.IO

  @Provides @Presentation fun presentationContext(): CoroutineContext = Dispatchers.Unconfined

  @Provides fun promptDuration(): Duration = Duration(3.5.seconds)
}
