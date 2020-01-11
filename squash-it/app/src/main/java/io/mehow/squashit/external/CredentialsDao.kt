package io.mehow.squashit.external

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Query

@Dao
interface CredentialsDao {
  @Query("SELECT * FROM credentials WHERE id = :id")
  fun select(id: String): Cursor
}
