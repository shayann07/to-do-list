package com.shayan.reminderstdl.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.data.models.User
import com.shayan.reminderstdl.data.repository.Repository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)
    val taskCreationStatus = MutableLiveData<Boolean>()
    val tasksList = MutableLiveData<List<Tasks>>()
    val todayTaskCount = MutableLiveData<Int>()
    val morningTasksLiveData = MutableLiveData<List<Tasks>>()
    val afternoonTasksLiveData = MutableLiveData<List<Tasks>>()
    val tonightTasksLiveData = MutableLiveData<List<Tasks>>()

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
                val todayTasks = tasks.filter { task ->
                    task.date == todayDate // Filter by today's date
                }

                tasksList.postValue(todayTasks)
                todayTaskCount.postValue(todayTasks.size)

                // Categorize tasks based on time (morning, afternoon, tonight)
                val morningTasks = mutableListOf<Tasks>()
                val afternoonTasks = mutableListOf<Tasks>()
                val tonightTasks = mutableListOf<Tasks>()

                val timeFormatter = SimpleDateFormat("hh:mm", Locale.getDefault()) // 12-hour format

                todayTasks.forEach { task ->
                    try {
                        // Parse the task time (assuming it's in hh:mm format)
                        val taskDate = timeFormatter.parse(task.time)
                        val taskTime = LocalTime.of(taskDate!!.hours, taskDate.minutes)
                        Log.d("ViewModel", "Task time: ${task.time} parsed as $taskTime")

                        // Categorize based on time
                        when {
                            taskTime.isBefore(LocalTime.of(12, 0)) -> {
                                morningTasks.add(task)
                                Log.d("ViewModel", "Morning task: ${task.time}")
                            }

                            taskTime.isAfter(
                                LocalTime.of(
                                    11, 59
                                )
                            ) && taskTime.isBefore(LocalTime.of(18, 0)) -> {
                                afternoonTasks.add(task)
                                Log.d("ViewModel", "Afternoon task: ${task.time}")
                            }

                            else -> {
                                tonightTasks.add(task)
                                Log.d("ViewModel", "Tonight task: ${task.time}")
                            }
                        }
                    } catch (e: Exception) {
                        // Handle invalid time format or other exceptions
                        Log.e("ViewModel", "Error parsing task time: ${task.time}", e)
                    }
                }

                // Post categorized tasks
                morningTasksLiveData.postValue(morningTasks)
                afternoonTasksLiveData.postValue(afternoonTasks)
                tonightTasksLiveData.postValue(tonightTasks)

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
