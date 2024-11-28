package com.shayan.reminderstdl.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shayan.reminderstdl.data.models.Tasks

@Dao
interface TasksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Tasks)

    @Query("SELECT * FROM tasks_table WHERE title LIKE :title")
    suspend fun getTaskByTitle(title: String): List<Tasks>

    @Query("DELETE FROM tasks_table")
    suspend fun clearAllTasks()

}