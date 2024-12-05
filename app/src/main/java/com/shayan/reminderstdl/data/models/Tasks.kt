package com.shayan.reminderstdl.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "tasks_table")
data class Tasks(
    @PrimaryKey(autoGenerate = false) val  firebaseTaskId: String = "",
    val title: String = "",
    val notes: String? = null,
    val date: String? = null,
    val time: String? = null,
    val timeCategory: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String? = null,
    val flag: Boolean = false,
    @get:PropertyName("completed") @set:PropertyName("completed") var isCompleted: Boolean = false
)
