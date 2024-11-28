package com.shayan.reminderstdl.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shayan.reminderstdl.data.local.dao.TasksDao
import com.shayan.reminderstdl.data.local.dao.UserDao
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.data.models.User


@Database(entities = [User::class, Tasks::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tasksDao(): TasksDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "app_database"
                )
                    .fallbackToDestructiveMigration() // Clears and rebuilds the database on version mismatch
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}