package com.shayan.reminderstdl.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shayan.reminderstdl.utils.Notification

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Pending Task Reminder"
        Notification.showTaskNotification(context, taskTitle)
    }
}