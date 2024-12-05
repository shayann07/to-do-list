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

class Repository(context: Context) {

    private val database = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val taskDao = AppDatabase.getInstance(context).tasksDao()


//    ROOM OPERATIONS

    // Save Task to Room
    suspend fun saveTasksToRoom(task: Tasks): Result<Boolean> {
        return try {
            val existingTask = taskDao.getTaskByFirebaseTaskId(task.firebaseTaskId.toString())
            if (existingTask == null) {
                taskDao.insertTask(task)
                Result.success(true)
            } else {
                Result.failure(Exception("Duplicate task Id detected"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save task: ${e.localizedMessage}"))
        }
    }

    // Fetch tasks for today from Room and their count
    suspend fun getTasksForToday(todayDate: String): List<Tasks> =
        taskDao.getTasksForToday(todayDate)

    fun getTodayTaskCountFlow(todayDate: String): Flow<Int> = taskDao.getTodayTaskCount(todayDate)

    // sets isCompleted == true in room
    // Update task completion locally
    suspend fun updateLocalTaskCompletion(firebaseTaskId: String, isCompleted: Boolean) {
        taskDao.updateTaskCompletion(firebaseTaskId, isCompleted)
        val updatedTask = taskDao.getTaskByFirebaseTaskId(firebaseTaskId)
        Log.d("Repository", "Updated Task: $updatedTask")
    }

    // Fetch completed tasks from Room and their count
    suspend fun getCompletedTasks(): List<Tasks> = taskDao.getCompletedTasks()
    fun getCompletedTasksCountFlow(): Flow<Int> = taskDao.getCompletedTaskCount()

    // Fetch (all.whereNotEqual("completed", true)) tasks from Room and their count
    suspend fun getIncompleteTasks(): List<Tasks> = taskDao.getIncompleteTasks()
    fun getIncompleteTasksCountFlow(): Flow<Int> = taskDao.getIncompleteTaskCount()

    // Update a task
    suspend fun updateTask(task: Tasks) {
        taskDao.updateTask(task)
    }

    // Clear all tasks locally
    suspend fun clearAllTasks() {
        taskDao.clearAllTasks()
    }

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

    // Save task to Firebase
    suspend fun saveTaskToFirebase(uid: String, task: Tasks): Result<String> {
        return try {
            val userTasksRef =
                database.collection("Users").document(uid).collection("Tasks").add(task).await()
            val firebaseTaskId = userTasksRef.id
            userTasksRef.update("firebaseTaskId", firebaseTaskId).await()
            Result.success(firebaseTaskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Fetch Tasks from Firebase
    suspend fun fetchTasksFromFirebase(uid: String): Result<List<Tasks>> {
        return try {
            val tasksSnapshot =
                database.collection("Users").document(uid).collection("Tasks").get().await()

            val tasksList = tasksSnapshot.documents.mapNotNull { document ->
                document.toObject(Tasks::class.java)?.copy(firebaseTaskId = document.id)
            }

            tasksList.forEach { saveTasksToRoom(it) } // Save tasks locally
            Result.success(tasksList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //    sets isCompleted == true in firebase
    suspend fun updateFirebaseTaskCompletion(
        firebaseTaskId: String, isCompleted: Boolean
    ): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val taskRef = database.collection("Users").document(uid).collection("Tasks")
                .document(firebaseTaskId)
            taskRef.update("completed", isCompleted).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Toggle task completion status in Firebase and Room
    suspend fun toggleTaskCompletion(
        firebaseTaskId: String, isCompleted: Boolean
    ): Result<Boolean> {
        return try {
            val firebaseResult = updateFirebaseTaskCompletion(firebaseTaskId, isCompleted)
            if (firebaseResult.isSuccess) {
                updateLocalTaskCompletion(firebaseTaskId, isCompleted)
                Result.success(true)
            } else {
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
