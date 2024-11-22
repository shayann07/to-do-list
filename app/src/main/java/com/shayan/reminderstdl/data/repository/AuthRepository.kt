package com.shayan.reminderstdl.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shayan.reminderstdl.data.models.ModelUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val database = FirebaseFirestore.getInstance()

    // Login method for Firestore
    suspend fun loginUser(phone: String, password: String): Result<ModelUser> {
        return try {
            val querySnapshot = database.collection("User")
                .whereEqualTo("phone", phone)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val userDocument = querySnapshot.documents[0]
                val user = userDocument.toObject(ModelUser::class.java)
                if (user != null && user.password == password) {
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
        firstName: String, lastName: String, phone: String, password: String
    ): Result<Unit> {
        return try {
            val modelUser = ModelUser(firstName, lastName, phone, password)
            database.collection("User").add(modelUser).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Registration failed: ${e.localizedMessage}"))
        }
    }
}
