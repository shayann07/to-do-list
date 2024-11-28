package com.shayan.reminderstdl.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_table")
data class Tasks(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val notes: String? = null,
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val flag: Boolean = false
)
