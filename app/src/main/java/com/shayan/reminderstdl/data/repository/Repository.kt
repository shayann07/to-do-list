package com.shayan.reminderstdl.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shayan.reminderstdl.data.local.AppDatabase
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Repository(context: Context) {

    private val database = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val taskDao = AppDatabase.getInstance(context).tasksDao()

    suspend fun getTaskCountByTitle(title: String): Int {
        return taskDao.countTaskByTitle(title)
    }


    // Fetch tasks for today from Room
    suspend fun getTasksFromRoom(todayDate: String): List<Tasks> {
        return taskDao.getTasksForToday(todayDate)
    }

    suspend fun getTaskByTitle(title: String): List<Tasks> {
        return taskDao.getTaskByTitle("%$title%") // Use SQL LIKE syntax for partial match
    }


    fun getTodayTaskCountFlow(todayDate: String): kotlinx.coroutines.flow.Flow<Int> {
        return taskDao.getTodayTaskCount(todayDate)
    }

    suspend fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val task = taskDao.getTaskById(taskId) ?: throw Exception("Task not found")
                taskDao.updateTask(task.copy(isCompleted = isCompleted))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to toggle task completion: ${e.localizedMessage}"))
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

    // Save Task to Firebase
    suspend fun saveTasksToFirebase(uid: String, task: Tasks): Result<String> {
        return try {
            val userTasksRef = database.collection("Users").document(uid).collection("Tasks")
            val taskRef = userTasksRef.add(task).await()
            Result.success(taskRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add task to Firebase: ${e.localizedMessage}"))
        }
    }

    // Save Task to Room
    suspend fun saveTasksToRoom(task: Tasks): Result<Boolean> {
        return try {
            val existingTask = taskDao.getTaskByTitle(task.title)
            if (existingTask.isEmpty()) {
                taskDao.insertTask(task)
                Result.success(true)
            } else {
                Result.failure(Exception("Duplicate task title detected"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save task: ${e.localizedMessage}"))
        }
    }

    // Fetch Tasks from Firebase
    suspend fun fetchTasksFromFirebase(uid: String): Result<List<Tasks>> {
        return try {
            val tasksSnapshot =
                database.collection("Users").document(uid).collection("Tasks").get().await()

            val tasksList = tasksSnapshot.documents.mapNotNull { document ->
                val title = document.getString("title")?.takeIf { it.isNotEmpty() }
                    ?: return@mapNotNull null
                val notes = document.getString("notes")
                val date = document.getString("date")
                val time = document.getString("time")
                val location = document.getString("location")
                val flag = document.getBoolean("flag") ?: false
                val timeCategory = document.getString("timeCategory")

                Tasks(
                    id = 0, // Room generates the ID
                    title = title,
                    notes = notes,
                    date = date,
                    time = time,
                    timeCategory = timeCategory,
                    location = location,
                    flag = flag
                )
            }

            tasksList.forEach { task -> saveTasksToRoom(task) } // Save locally
            Result.success(tasksList)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch tasks: ${e.localizedMessage}"))
        }
    }

    // Save User Locally
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
}
