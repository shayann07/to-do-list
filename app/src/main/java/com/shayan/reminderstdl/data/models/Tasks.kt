package com.shayan.reminderstdl.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tasks_table")
data class Tasks(
    @PrimaryKey(autoGenerate = false) val firebaseTaskId: String = "",
    val title: String = "",
    val notes: String? = null,
    val date: String? = null,
    val dateCompleted: String? = null,
    val time: String? = null,
    val timeCategory: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val flag: Boolean = false,
    @get:PropertyName("completed") @set:PropertyName("completed") var isCompleted: Boolean = false
) : Parcelable