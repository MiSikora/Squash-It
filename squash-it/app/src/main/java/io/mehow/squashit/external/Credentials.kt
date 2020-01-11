package io.mehow.squashit.external

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Credentials(@PrimaryKey val id: String, val secret: String)
