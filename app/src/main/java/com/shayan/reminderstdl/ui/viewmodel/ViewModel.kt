package com.shayan.reminderstdl.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
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
    val morningTasksLiveData = MutableLiveData<List<Tasks>>()
    val afternoonTasksLiveData = MutableLiveData<List<Tasks>>()
    val tonightTasksLiveData = MutableLiveData<List<Tasks>>()
    val todayTaskCount: MutableLiveData<Int> = MutableLiveData()


    fun isDuplicateTitle(title: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val count = repository.getTaskCountByTitle(title)
            callback(count > 0)
        }
    }


    fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleTaskCompletion(taskId, isCompleted)
            if (result.isSuccess) {
                // Update only Room data and LiveData, avoid Firebase calls
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayTasks = repository.getTasksFromRoom(todayDate)
                val (morningTasks, afternoonTasks, tonightTasks) = categorizeTasksByTime(todayTasks)

                morningTasksLiveData.postValue(morningTasks)
                afternoonTasksLiveData.postValue(afternoonTasks)
                tonightTasksLiveData.postValue(tonightTasks)
            } else {
                Log.e(
                    "ViewModel",
                    "Error toggling task completion: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }


    fun observeTodayTaskCount() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            repository.getTodayTaskCountFlow(todayDate).collect { count ->
                todayTaskCount.postValue(count)
            }
        }
    }

    fun markTaskAsCompleted(taskId: Int) {
        viewModelScope.launch {
            repository.updateTaskCompletion(taskId, true)
        }
    }

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

    fun checkDuplicateTaskTitle(title: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val taskRef = db.collection("tasks")
        taskRef.whereEqualTo("title", title).get().addOnSuccessListener { querySnapshot ->
            // If there's any document with the same title, return true
            callback(querySnapshot.isEmpty.not())
        }.addOnFailureListener {
            // Handle failure case, maybe log or show an error
            callback(false)
        }
    }


    fun fetchTasks(uid: String) {
        viewModelScope.launch {
            tasksList.postValue(emptyList())
            val result = repository.fetchTasksFromFirebase(uid)
            result.fold(onSuccess = { tasks ->
                // Save only new tasks locally
                tasks.forEach { task ->
                    val existingTask = repository.getTaskByTitle(task.title).firstOrNull()
                    if (existingTask == null) {
                        repository.saveTasksToRoom(task)
                    }
                }

                // Get today's date
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Fetch today's tasks
                val todayTasks = repository.getTasksFromRoom(todayDate)

                // Update LiveData
                tasksList.postValue(todayTasks)
                todayTaskCount.postValue(todayTasks.size)

                val (morningTasks, afternoonTasks, tonightTasks) = categorizeTasksByTime(todayTasks)
                morningTasksLiveData.postValue(morningTasks)
                afternoonTasksLiveData.postValue(afternoonTasks)
                tonightTasksLiveData.postValue(tonightTasks)
            }, onFailure = { exception ->
                Log.e("ViewModel", "Failed to fetch tasks: ${exception.message}")
            })
        }
    }


    // Helper function to categorize tasks
    private fun categorizeTasksByTime(tasks: List<Tasks>): Triple<List<Tasks>, List<Tasks>, List<Tasks>> {
        val morningTasks = mutableListOf<Tasks>()
        val afternoonTasks = mutableListOf<Tasks>()
        val tonightTasks = mutableListOf<Tasks>()
        val timeFormatter = SimpleDateFormat("hh:mm", Locale.getDefault())

        tasks.forEach { task ->
            try {
                val taskDate = timeFormatter.parse(task.time)
                val taskTime = LocalTime.of(taskDate!!.hours, taskDate.minutes)

                when {
                    taskTime.isBefore(LocalTime.of(12, 0)) -> morningTasks.add(task)
                    taskTime.isAfter(LocalTime.of(11, 59)) && taskTime.isBefore(
                        LocalTime.of(
                            18, 0
                        )
                    ) -> afternoonTasks.add(task)

                    else -> tonightTasks.add(task)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error parsing task time: ${task.time}", e)
            }
        }
        return Triple(morningTasks, afternoonTasks, tonightTasks)
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
