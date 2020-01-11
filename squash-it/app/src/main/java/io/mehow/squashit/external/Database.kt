package io.mehow.squashit.external

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Credentials::class], version = 1)
abstract class Database : RoomDatabase() {
  abstract fun credentialsDao(): CredentialsDao
}
