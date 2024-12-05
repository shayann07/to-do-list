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

    val tasksList = MutableLiveData<List<Tasks>?>()
    val todayTaskCount: MutableLiveData<Int> = MutableLiveData()
    val completedTasks = MutableLiveData<List<Tasks>>()
    val incompleteTasks = MutableLiveData<List<Tasks>>()
    val taskCreationStatus = MutableLiveData<Boolean>()
    val morningTasksLiveData = MutableLiveData<List<Tasks>>()
    val afternoonTasksLiveData = MutableLiveData<List<Tasks>>()
    val tonightTasksLiveData = MutableLiveData<List<Tasks>>()

    // Fetch today's tasks and categorize them
    fun fetchTodayTasks() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            try {
                val tasks = repository.getTasksForToday(todayDate)
                val sortedTasks = tasks.sortedByDescending { it.timestamp }
                val incompleteTasksForToday =
                    sortedTasks.filter { !it.isCompleted } // Ensure exclusion of completed tasks

                tasksList.postValue(incompleteTasksForToday)
                todayTaskCount.postValue(incompleteTasksForToday.size)

                val (morning, afternoon, tonight) = categorizeTasksByTime(incompleteTasksForToday)
                morningTasksLiveData.postValue(morning)
                afternoonTasksLiveData.postValue(afternoon)
                tonightTasksLiveData.postValue(tonight)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch tasks for today: ${e.message}")
            }
        }
    }

    // Fetch all incomplete tasks and observe in fragments
    fun fetchIncompleteTasks() {
        viewModelScope.launch {
            try {
                val tasks = repository.getIncompleteTasks()
                incompleteTasks.postValue(tasks)
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to fetch incomplete tasks: ${e.message}")
            }
        }
    }


    // Toggle task completion status
    fun toggleTaskCompletion(
        firebaseTaskId: String, isCompleted: Boolean, onCompletion: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.toggleTaskCompletion(firebaseTaskId, isCompleted)
                result.fold(onSuccess = {
                    // Fetch only incomplete tasks again to update the UI
                    val todayDate =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val tasks = repository.getTasksForToday(todayDate)
                    val incompleteTasks = tasks.filter { !it.isCompleted }

                    tasksList.postValue(incompleteTasks)
                    todayTaskCount.postValue(incompleteTasks.size)

                    val (morning, afternoon, tonight) = categorizeTasksByTime(incompleteTasks)
                    morningTasksLiveData.postValue(morning)
                    afternoonTasksLiveData.postValue(afternoon)
                    tonightTasksLiveData.postValue(tonight)

                    onCompletion(true, "Task successfully updated!")
                }, onFailure = { exception ->
                    onCompletion(false, "Failed to update task: ${exception.message}")
                })
            } catch (e: Exception) {
                onCompletion(false, "Unexpected error: ${e.message}")
            }
        }
    }


    // Save a new task to Firebase and Room
    fun saveTask(uid: String, task: Tasks) {
        viewModelScope.launch {
            try {
                val firebaseResult = repository.saveTaskToFirebase(
                    uid, task.copy(timestamp = System.currentTimeMillis())
                )
                firebaseResult.fold(onSuccess = { firebaseTaskId ->
                    val updatedTask = task.copy(
                        firebaseTaskId = firebaseTaskId, timestamp = System.currentTimeMillis()
                    )
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

    // Fetch tasks from Firebase and save them to Room
    fun fetchTasks(uid: String) {
        viewModelScope.launch {
            try {
                val result = repository.fetchTasksFromFirebase(uid)
                result.fold(onSuccess = { tasks ->
                    val todayDate =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val todayTasks = tasks.filter { it.date == todayDate && !it.isCompleted }
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

    // Categorize tasks based on their time
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

    // Login user
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

    // Register user
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
