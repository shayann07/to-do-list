package com.shayan.reminderstdl.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shayan.reminderstdl.data.models.ModelUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val database = FirebaseFirestore.getInstance()

    suspend fun loginUser(phone: String, pass: String): ModelUser? {

        val querySnapshot = database.collection("User").whereEqualTo("phone", phone).get().await()

        if (querySnapshot.isEmpty) {
            for (document in querySnapshot) {
                val user = document.toObject(ModelUser::class.java)
                if (user.password == pass) {
                    return user
                }
            }
        }
        return null
    }

    suspend fun registerUser(
        firstName: String, lastName: String, phone: String, pass: String, cPass: String
    ): Boolean {

        val modelUser = ModelUser(firstName, lastName, phone, pass, cPass)
        return try {
            database.collection("User").add(modelUser).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}