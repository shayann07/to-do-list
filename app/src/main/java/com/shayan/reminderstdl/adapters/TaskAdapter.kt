package com.shayan.reminderstdl.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.ReminderItemsBinding

// TaskAdapter now extends ListAdapter for better performance and automatic updates
class TaskAdapter : ListAdapter<Tasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            ReminderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    // ViewHolder to hold the view bindings
    class TaskViewHolder(private val binding: ReminderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Tasks) {
            binding.fetchedTaskTitle.text = task.title
            binding.fetchedTaskTime.text = task.time ?: "Time not available"
        }
    }

    // DiffUtil callback to improve list update performance
    class TaskDiffCallback : DiffUtil.ItemCallback<Tasks>() {
        override fun areItemsTheSame(oldItem: Tasks, newItem: Tasks): Boolean {
            return oldItem.id == newItem.id // Compare based on unique ID
        }

        override fun areContentsTheSame(oldItem: Tasks, newItem: Tasks): Boolean {
            return oldItem == newItem // Compare content
        }
    }
}
