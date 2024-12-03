package com.shayan.reminderstdl.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.ReminderItemsBinding

class TaskAdapter(private val listener: TaskCompletionListener) :
    ListAdapter<Tasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            ReminderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task, listener)
    }

    class TaskViewHolder(private val binding: ReminderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Tasks, listener: TaskCompletionListener) {
            binding.fetchedTaskTitle.text = task.title
            binding.fetchedTaskTime.text = task.time ?: "Time not available"
            binding.radioButton.isChecked = task.isCompleted

            // Toggle completion state
            binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
                listener.onTaskCompletionToggled(task.id, isChecked)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Tasks>() {
        override fun areItemsTheSame(oldItem: Tasks, newItem: Tasks): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tasks, newItem: Tasks): Boolean {
            return oldItem == newItem
        }
    }

    interface TaskCompletionListener {
        fun onTaskCompletionToggled(taskId: Int, isCompleted: Boolean)
    }
}
