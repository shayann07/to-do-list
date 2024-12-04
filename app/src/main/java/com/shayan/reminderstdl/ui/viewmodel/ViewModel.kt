package com.shayan.reminderstdl.ui.viewmodel

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
import java.util.*

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)

    val tasksList = MutableLiveData<List<Tasks>>()
    val todayTaskCount: MutableLiveData<Int> = MutableLiveData()
    val completedTasks = MutableLiveData<List<Tasks>>()
    val incompleteTasks = MutableLiveData<List<Tasks>>()
    val taskCreationStatus = MutableLiveData<Boolean>()
    val morningTasksLiveData = MutableLiveData<List<Tasks>>()
    val afternoonTasksLiveData = MutableLiveData<List<Tasks>>()
    val tonightTasksLiveData = MutableLiveData<List<Tasks>>()

    fun fetchTodayTasks() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            try {
                val tasks = repository.getTasksForToday(todayDate)
                tasksList.postValue(tasks)
                todayTaskCount.postValue(tasks.size)

                val (morning, afternoon, tonight) = categorizeTasksByTime(tasks)
                morningTasksLiveData.postValue(morning)
                afternoonTasksLiveData.postValue(afternoon)
                tonightTasksLiveData.postValue(tonight)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch tasks for today: ${e.message}")
            }
        }
    }

    fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val task = repository.getTaskById(taskId)
                if (task != null) {
                    val firebaseTaskId = task.firebaseTaskId
                    repository.updateLocalTaskCompletion(taskId, isCompleted)
                    if (!firebaseTaskId.isNullOrEmpty()) {
                        repository.updateFirebaseTaskCompletion(firebaseTaskId, isCompleted)
                    }
                    fetchTodayTasks()
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to toggle task completion: ${e.message}")
            }
        }
    }

    fun saveTask(uid: String, task: Tasks) {
        viewModelScope.launch {
            try {
                val firebaseResult = repository.saveTasksToFirebase(uid, task)
                firebaseResult.fold(onSuccess = { firebaseTaskId ->
                    val updatedTask = task.copy(firebaseTaskId = firebaseTaskId)
                    val roomResult = repository.saveTasksToRoom(updatedTask)
                    taskCreationStatus.postValue(roomResult.isSuccess)
                }, onFailure = {
                    taskCreationStatus.postValue(false)
                })
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to save task: ${e.message}")
            }
        }
    }

    fun fetchTasks(uid: String) {
        viewModelScope.launch {
            try {
                val result = repository.fetchTasksFromFirebase(uid)
                result.fold(onSuccess = { tasks ->
                    val todayDate =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val todayTasks = tasks.filter { it.date == todayDate }
                    tasksList.postValue(todayTasks)
                    todayTaskCount.postValue(todayTasks.size)

                    val (morning, afternoon, tonight) = categorizeTasksByTime(todayTasks)
                    morningTasksLiveData.postValue(morning)
                    afternoonTasksLiveData.postValue(afternoon)
                    tonightTasksLiveData.postValue(tonight)
                }, onFailure = { exception ->
                    Log.e("ViewModel", "Failed to fetch tasks: ${exception.message}")
                })
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch tasks: ${e.message}")
            }
        }
    }

    private fun categorizeTasksByTime(tasks: List<Tasks>): Triple<List<Tasks>, List<Tasks>, List<Tasks>> {
        val morning = mutableListOf<Tasks>()
        val afternoon = mutableListOf<Tasks>()
        val tonight = mutableListOf<Tasks>()
        tasks.forEach { task ->
            val time = task.time?.split(":")?.firstOrNull()?.toIntOrNull() ?: return@forEach
            when (time) {
                in 5..11 -> morning.add(task)
                in 12..16 -> afternoon.add(task)
                else -> tonight.add(task)
            }
        }
        return Triple(morning, afternoon, tonight)
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


}
