package io.mehow.squashit

import android.content.Context
import androidx.room.Room
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.time.seconds
import io.mehow.squashit.external.Database as RoomDatabase

@Module
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

  @Provides @Singleton fun roomDatabase(context: Context): RoomDatabase {
    return Room.databaseBuilder(context, RoomDatabase::class.java, DbName).build()
  }

  @Provides fun credentialsDao(database: RoomDatabase) = database.credentialsDao()
}
