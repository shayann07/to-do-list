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

    suspend fun saveTasksToFirebase(uid: String, task: Tasks): Result<Boolean> {
        return try {
            val userTasksRef = database.collection("Users").document(uid).collection("Tasks")
            val taskMap = mapOf(
                "title" to task.title,
                "notes" to task.notes,
                "date" to task.date,
                "time" to task.time,
                "timeCategory" to task.timeCategory,  // Added timeCategory
                "location" to task.location,
                "flag" to task.flag
            )
            userTasksRef.add(task).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add task to Firebase: ${e.localizedMessage}"))
        }
    }

    suspend fun saveTasksToRoom(task: Tasks): Result<Boolean> {
        return try {
            taskDao.insertTask(task)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add task to Room: ${e.localizedMessage}"))
        }
    }

    private suspend fun saveUserLocally(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    suspend fun fetchTasksFromFirebase(uid: String): Result<List<Tasks>> {

        return try {
            val tasksSnapshot =
                database.collection("Users").document(uid).collection("Tasks").get().await()

            val tasksList = tasksSnapshot.documents.mapNotNull { document ->
                // Get the title safely, returning null if missing or empty
                val title = document.getString("title")?.takeIf { it.isNotEmpty() }
                    ?: return@mapNotNull null  // Skip this task if title is empty or missing

                val notes = document.getString("notes")
                val date = document.getString("date")
                val time =
                    document.getString("time")?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val location = document.getString("location")
                val flag = document.getBoolean("flag") ?: false
                val timeCategory = document.getString("timeCategory")  // Retrieve timeCategory

                Tasks(
                    title = title,
                    notes = notes,
                    date = date,
                    time = time,
                    timeCategory = timeCategory,  // Populate timeCategory
                    location = location,
                    flag = flag
                )

            }

            if (tasksList.isNotEmpty()) {
                Result.success(tasksList)
            } else {
                Result.failure(Exception("No valid tasks found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch tasks: ${e.localizedMessage}"))
        }
    }


    suspend fun getLocalUser(email: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByEmail(email)
        }
    }

    suspend fun clearLocalUsers() {
        withContext(Dispatchers.IO) {
            userDao.clearAllUsers()
        }
    }
}
