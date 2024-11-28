package com.shayan.reminderstdl.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = TaskRepository(application)

    val taskCreationStatus = MutableLiveData<Boolean>()

    fun saveTask(userPhone: String, task: Tasks) {
        viewModelScope.launch {
            val firebaseResult = taskRepository.saveTasksToFirebase(userPhone, task)
            if (firebaseResult.isSuccess) {
                val roomResult = taskRepository.saveTasksToRoom(task)
                taskCreationStatus.postValue(roomResult.isSuccess)
            } else {
                taskCreationStatus.postValue(false)
            }

        }
    }
}