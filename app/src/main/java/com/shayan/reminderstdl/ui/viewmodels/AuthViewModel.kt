package com.shayan.reminderstdl.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shayan.reminderstdl.data.models.ModelUser
import com.shayan.reminderstdl.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = AuthRepository()
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("PrefsDatabase", Context.MODE_PRIVATE)

    fun login(
        phone: String, password: String, onSuccess: (ModelUser) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.loginUser(phone, password)
            result.fold(onSuccess = { user ->
                saveUserPreferences(user)
                onSuccess(user)
            }, onFailure = { exception ->
                onError(exception.message ?: "An error occurred during login")
            })
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        phone: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (password != confirmPassword) {
            onError("Passwords do not match")
            return
        }

        viewModelScope.launch {
            val result = authRepository.registerUser(firstName, lastName, phone, password)
            result.fold(onSuccess = { onSuccess() }, onFailure = { exception ->
                onError(
                    exception.message ?: "An error occurred during registration"
                )
            })
        }
    }

    private fun saveUserPreferences(user: ModelUser) {
        sharedPreferences.edit().apply {
            putString("first_name", user.firstName)
            putString("last_name", user.lastName)
            putString("phone", user.phone)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun logout() {
        sharedPreferences.edit().apply {
            putBoolean("is_logged_in", false)
            apply()
        }
    }
}
