package com.shayan.reminderstdl.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Pending Task Reminder"
        Notification.showTaskNotification(context, taskTitle)
    }
}