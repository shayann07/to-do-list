package com.shayan.reminderstdl.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.shayan.reminderstdl.data.local.AppDatabase
import com.shayan.reminderstdl.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {

    private val database = FirebaseFirestore.getInstance()
    private val userDao = AppDatabase.getInstance(context).userDao()

    // Login method for Firestore
    suspend fun loginUser(phone: String, password: String): Result<User> {
        return try {
            val querySnapshot =
                database.collection("User").whereEqualTo("phone", phone).get().await()

            if (!querySnapshot.isEmpty) {
                val userDocument = querySnapshot.documents[0]
                val user = userDocument.toObject(User::class.java)
                if (user != null && user.password == password) {
                    saveUserLocally(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Incorrect password"))
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.localizedMessage}"))
        }
    }

    // Registration method for Firestore
    suspend fun registerUser(
        user: User
    ): Result<Unit> {
        return try {

            database.collection("User").add(user).await()
            saveUserLocally(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed: ${e.localizedMessage}"))
        }
    }

    private suspend fun saveUserLocally(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    suspend fun getLocalUser(phone: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByPhone(phone)
        }
    }

    suspend fun clearLocalUsers() {
        withContext(Dispatchers.IO) {
            userDao.clearAllUsers()
        }
    }


}
