package com.shayan.reminderstdl.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shayan.reminderstdl.data.models.Tasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Tasks)

    @Query("SELECT COUNT(*) FROM tasks_table WHERE title = :title")
    suspend fun countTaskByTitle(title: String): Int

    @Query("SELECT * FROM tasks_table WHERE date = :todayDate AND isCompleted = 0")
    suspend fun getTasksForToday(todayDate: String): List<Tasks>

    @Query("SELECT * FROM tasks_table WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): Tasks?

    @Query("SELECT * FROM tasks_table WHERE title LIKE :title")
    suspend fun getTaskByTitle(title: String): List<Tasks>

    @Query("SELECT COUNT(*) FROM tasks_table WHERE date = :todayDate AND isCompleted = 0")
    fun getTodayTaskCount(todayDate: String): Flow<Int>

    @Query("SELECT * FROM tasks_table WHERE isCompleted = 1")
    suspend fun getCompletedTasks(): List<Tasks>

    @Query("UPDATE tasks_table SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)

    @Update
    fun updateTask(task: Tasks)

    @Query("DELETE FROM tasks_table")
    suspend fun clearAllTasks()
}