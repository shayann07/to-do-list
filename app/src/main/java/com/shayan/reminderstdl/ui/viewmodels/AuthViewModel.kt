package com.shayan.reminderstdl.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shayan.reminderstdl.data.models.ModelUser
import com.shayan.reminderstdl.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository, private val sharedPreferences: SharedPreferences
) : ViewModel() {

    fun login(
        phone: String, pass: String, onSuccess: (ModelUser) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = authRepository.loginUser(phone, pass)
                if (user != null) {
                    // Save user info to SharedPreferences on success
                    saveUserPreferences(user.phone, true)

                    onSuccess(user)
                } else {
                    onError("Invalid credentials")
                }

            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }

    private fun saveUserPreferences(phone: String, isLoggedIn: Boolean) {
        sharedPreferences.edit().apply {
            putString("user_phone", phone)
            putBoolean("is_logged_in", isLoggedIn)
            apply()
        }
    }

    fun logout() {
        sharedPreferences.edit().apply {
            putBoolean("is_logged_in", false)
            apply()
        }
    }


    fun register(
        firstName: String,
        lastName: String,
        phone: String,
        pass: String,
        cPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch {
            try {
                val isSuccess = authRepository.registerUser(firstName, lastName, phone, pass, cPass)
                if (isSuccess) {
                    onSuccess()
                } else {
                    onError("Failed to register user")
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }

    }


}