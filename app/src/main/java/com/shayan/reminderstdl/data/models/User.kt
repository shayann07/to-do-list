package com.shayan.reminderstdl.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String? = null,
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
