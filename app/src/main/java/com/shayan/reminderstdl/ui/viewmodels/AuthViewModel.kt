package com.shayan.reminderstdl.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shayan.reminderstdl.data.models.User
import com.shayan.reminderstdl.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    fun login(
        phone: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.loginUser(phone, password)
            result.fold(onSuccess = { user ->
                onSuccess(user)
            }, onFailure = { exception ->
                onError(exception.message ?: "An error occurred during login")
            })
        }
    }

    fun register(
        user: User, onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.registerUser(user)
            result.fold(onSuccess = { onSuccess() }, onFailure = { exception ->
                onError(
                    exception.message ?: "An error occurred during registration"
                )
            })
        }
    }

    fun fetchLocalUser(phone: String, onSuccess: (User?) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.getLocalUser(phone)
            onSuccess(user)
        }
    }
}
