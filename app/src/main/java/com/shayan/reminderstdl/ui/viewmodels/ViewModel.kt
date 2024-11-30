package com.shayan.reminderstdl.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.data.models.User
import com.shayan.reminderstdl.data.repository.Repository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)
    val taskCreationStatus = MutableLiveData<Boolean>()
    val tasksList = MutableLiveData<List<Tasks>>()
    val todayTaskCount = MutableLiveData<Int>()

    fun saveTask(uid: String, task: Tasks) {
        viewModelScope.launch {
            val firebaseResult = repository.saveTasksToFirebase(uid, task)
            if (firebaseResult.isSuccess) {
                val roomResult = repository.saveTasksToRoom(task)
                taskCreationStatus.postValue(roomResult.isSuccess)
            } else {
                taskCreationStatus.postValue(false)
            }
        }
    }


    fun fetchTasks(uid: String) {
        viewModelScope.launch {
            val result = repository.fetchTasksFromFirebase(uid)
            result.fold(onSuccess = { tasks ->

                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayTasks = tasks.filter { tasks ->
                    tasks.date == todayDate && tasks.time in listOf(
                        "morning", "afternoon", "tonight"
                    )
                }

                tasksList.postValue(todayTasks)
                todayTaskCount.postValue(todayTasks.size)

            }, onFailure = { exception ->
                // Handle the error
            })
        }
    }


    fun login(
        email: String, password: String, onSuccess: (User) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            result.fold(onSuccess = { user ->
                onSuccess(user)
            }, onFailure = { exception ->
                onError(exception.message ?: "An error occurred during login")
            })
        }
    }

    fun register(user: User, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {

        viewModelScope.launch {
            val result = repository.registerUser(user, password)
            result.fold(onSuccess = {
                onSuccess()
            }, onFailure = { exception ->
                onError(exception.message ?: "An error occurred during registration")
            })
        }
    }

    fun fetchLocalUser(email: String, onSuccess: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getLocalUser(email)
            onSuccess(user)
        }
    }
}
