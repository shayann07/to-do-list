package com.shayan.reminderstdl.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.shayan.reminderstdl.data.local.AppDatabase
import com.shayan.reminderstdl.data.models.Tasks
import kotlinx.coroutines.tasks.await

class TaskRepository(context: Context) {

    private val database = FirebaseFirestore.getInstance()
    private val tasksDao = AppDatabase.getInstance(context).tasksDao()

    suspend fun saveTasksToFirebase(userPhone: String, task: Tasks): Result<Boolean> {
        return try {
            val userTasksRef = database.collection("User").document(userPhone).collection("Tasks")
            userTasksRef.add(task).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add task to Firebase: ${e.localizedMessage}"))
        }
    }

    suspend fun saveTasksToRoom(task: Tasks): Result<Boolean> {
        return try {
            tasksDao.insertTask(task)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add task to Room: ${e.localizedMessage}"))
        }
    }
}