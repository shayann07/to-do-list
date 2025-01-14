package com.shayan.reminderstdl.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shayan.reminderstdl.data.local.AppDatabase
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(context: Context) {

    private val database = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val taskDao = AppDatabase.getInstance(context).tasksDao()


//    ROOM OPERATIONS

    suspend fun getTasksByTitle(title: String): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTasksByTitle(title) }

    // Save Task to Room
    suspend fun saveTasksToRoom(task: Tasks): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val existingTask = taskDao.getTaskByFirebaseTaskId(task.firebaseTaskId ?: "")
            if (existingTask == null) {
                taskDao.insertTask(task)
                Result.success(true)
            } else {
                Result.failure(Exception("Task with ID ${task.firebaseTaskId} already exists"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch Tasks for Today
    suspend fun getTasksForToday(todayDate: String): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTasksForToday(todayDate) }

    fun getTodayTaskCountFlow(todayDate: String): Flow<Int> = taskDao.getTodayTaskCount(todayDate)

    suspend fun getScheduledTasks(startDate: String, endDate: String): List<Tasks> =
        withContext(Dispatchers.IO) {
            taskDao.getTasksForDateRange(startDate, endDate)
        }

    fun getScheduledTasksCountFlow(startDate: String, endDate: String): Flow<Int> =
        taskDao.getTasksCountForDateRange(startDate, endDate)

    // Fetch Flagged Tasks
    suspend fun getFlaggedTasks(): List<Tasks> = withContext(Dispatchers.IO) {
        taskDao.getFlaggedTasks()
    }

    fun getFlaggedTaskCountFlow(): Flow<Int> = taskDao.getFlaggedTaskCount()

    // Update Task Completion Locally
    suspend fun updateLocalTaskCompletion(
        firebaseTaskId: String, isCompleted: Boolean, dateCompleted: String?
    ) = withContext(Dispatchers.IO) {
        taskDao.updateTaskCompletion(firebaseTaskId, isCompleted, dateCompleted)
        Log.d(
            "Repository",
            "Local task status updated: ID=$firebaseTaskId, isCompleted=$isCompleted, dateCompleted=$dateCompleted"
        )
    }


    // Fetch (all.whereNotEqual("completed", true)) tasks from Room and their count
    suspend fun getIncompleteTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getIncompleteTasks() }

    fun getIncompleteTasksCountFlow(): Flow<Int> = taskDao.getIncompleteTaskCount()

    // Fetch completed tasks from Room and their count
    suspend fun getCompletedTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getCompletedTasks() }

    fun getCompletedTasksCountFlow(): Flow<Int> = taskDao.getCompletedTaskCount()


    // Fetch every tasks from Room and its count
    suspend fun getTotalTasks(): List<Tasks> =
        withContext(Dispatchers.IO) { taskDao.getTotalTasks() }

    fun getTotalTasksCountFlow(): Flow<Int> = taskDao.getTotalTaskCount()

    // Update a task
    suspend fun updateTask(task: Tasks) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    // Delete task by ID
    suspend fun deleteTaskFromRoom(firebaseTaskId: String) =
        withContext(Dispatchers.IO) { taskDao.deleteTaskByFirebaseTaskId(firebaseTaskId) }

    // Clear Completed Tasks Locally
    suspend fun clearAllCompletedTasks() =
        withContext(Dispatchers.IO) { taskDao.clearAllCompletedTasks() }

    suspend fun clearAllTasks() = withContext(Dispatchers.IO) { taskDao.clearAllTasks() }

    //  Save User Locally
    private suspend fun saveUserLocally(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    // Get Local User
    suspend fun getLocalUser(email: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByEmail(email)
        }
    }

    // Clear Local Users
    suspend fun clearLocalUsers() {
        withContext(Dispatchers.IO) {
            userDao.clearAllUsers()
        }
    }


//        FIREBASE OPERATIONS

    // Save Task to Firebase
    suspend fun saveTaskToFirebase(uid: String, task: Tasks): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val documentRef =
                    database.collection("Users").document(uid).collection("Tasks").add(task).await()
                val firebaseTaskId = documentRef.id
                documentRef.update("firebaseTaskId", firebaseTaskId).await()
                Result.success(firebaseTaskId)
            } catch (e: Exception) {
                Log.e("Repository", "Failed to save task to Firebase: ${e.localizedMessage}")
                Result.failure(e)
            }
        }


    // Fetch Tasks from Firebase
    suspend fun fetchTasksFromFirebaseAndSaveToRoom(uid: String): Result<List<Tasks>> =
        withContext(Dispatchers.IO) {
            try {
                val tasksSnapshot =
                    database.collection("Users").document(uid).collection("Tasks").get().await()
                val tasksList = tasksSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Tasks::class.java)?.copy(firebaseTaskId = doc.id)
                }
                saveFetchedTasksToRoom(tasksList)
                Result.success(tasksList)
            } catch (e: Exception) {
                Log.e("Repository", "Failed to fetch tasks: ${e.localizedMessage}")
                Result.failure(e)
            }
        }

    private suspend fun saveFetchedTasksToRoom(tasks: List<Tasks>) {
        tasks.forEach { task ->
            val localTask = taskDao.getTaskByFirebaseTaskId(task.firebaseTaskId ?: "")
            if (localTask == null || localTask.isCompleted != task.isCompleted) {
                taskDao.insertTask(task)
            }
        }
    }

    // Update Firebase Task Completion
    suspend fun updateFirebaseTaskCompletion(
        firebaseTaskId: String, isCompleted: Boolean, dateCompleted: String?
    ): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val updates = mapOf(
                "completed" to isCompleted, "dateCompleted" to dateCompleted
            )
            database.collection("Users").document(uid).collection("Tasks").document(firebaseTaskId)
                .update(updates).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(
                "Repository", "Failed to update task completion in Firebase: ${e.localizedMessage}"
            )
            Result.failure(e)
        }
    }

    suspend fun deleteTaskFromFirebaseAndRoom(firebaseTaskId: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Delete from Firebase
            database.collection("Users").document(uid).collection("Tasks").document(firebaseTaskId)
                .delete().await()

            // Delete from Room
            deleteTaskFromRoom(firebaseTaskId)

            Result.success(true)
        } catch (e: Exception) {
            Log.e("Repository", "Failed to delete task: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deleteCompletedTasksFromFirebaseAndRoom(isCompleted: Boolean): Result<Boolean> {
        return try {
            // Check if the user is logged in
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Fetch completed tasks from Firebase
            val firebaseResult = database.collection("Users").document(uid).collection("Tasks")
                .whereEqualTo("completed", isCompleted).get().await()

            // Delete all completed tasks from Firebase
            for (document in firebaseResult.documents) {
                document.reference.delete().await()
            }

            // Delete all completed tasks from Room database
            clearAllCompletedTasks()

            // Log success
            Log.d("Repository", "Completed tasks deleted successfully from Firebase and Room")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("Repository", "Error in deleteCompletedTasksFromFirebaseAndRoom: ${e.message}")
            Result.failure(e)
        }
    }


    // Toggle task completion status in Firebase and Room
    suspend fun toggleTaskCompletion(
        firebaseTaskId: String, isCompleted: Boolean
    ): Result<Boolean> {
        return try {
            val dateCompleted = if (isCompleted) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            } else {
                null
            }
            val firebaseResult =
                updateFirebaseTaskCompletion(firebaseTaskId, isCompleted, dateCompleted)
            if (firebaseResult.isSuccess) {
                updateLocalTaskCompletion(firebaseTaskId, isCompleted, dateCompleted)
                Log.d(
                    "Repository",
                    "Task completion toggled successfully for ID: $firebaseTaskId with dateCompleted: $dateCompleted"
                )
                Result.success(true)
            } else {
                Log.e("Repository", "Failed to update task completion in Firebase")
                Result.failure(Exception("Failed to update task completion in Firebase"))
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error in toggleTaskCompletion: ${e.message}")
            Result.failure(e)
        }
    }


    // Register User
    suspend fun registerUser(user: User, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = auth.currentUser

            if (firebaseUser != null) {
                val userMap = mapOf(
                    "firstName" to user.firstName,
                    "lastName" to user.lastName,
                    "email" to user.email
                )
                database.collection("Users").document(firebaseUser.uid).set(userMap).await()
                saveUserLocally(user)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Firebase user creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed: ${e.localizedMessage}"))
        }
    }

    // Login User
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser

            if (firebaseUser != null) {
                val localUser = getLocalUser(email)
                if (localUser != null) {
                    Result.success(localUser)
                } else {
                    val userSnapshot =
                        database.collection("Users").document(firebaseUser.uid).get().await()
                    if (userSnapshot.exists()) {
                        val fetchedUser = User(
                            email = email,
                            firstName = userSnapshot.getString("firstName"),
                            lastName = userSnapshot.getString("lastName")
                        )
                        saveUserLocally(fetchedUser)
                        Result.success(fetchedUser)
                    } else {
                        Result.failure(Exception("User not found in Firebase"))
                    }
                }
            } else {
                Result.failure(Exception("Firebase authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.localizedMessage}"))
        }
    }
}