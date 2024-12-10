package com.shayan.reminderstdl.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.ReminderItemsBinding

class TaskAdapter(
    private val completionListener: TaskCompletionListener,
    private val itemClickListener: OnItemClickListener
) :
    ListAdapter<Tasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            ReminderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task, completionListener, itemClickListener)
    }

    class TaskViewHolder(private val binding: ReminderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            task: Tasks,
            completionListener: TaskCompletionListener,
            itemClickListener: OnItemClickListener
        ) {
            binding.fetchedTaskTitle.text = task.title
            binding.fetchedTaskTime.text = task.time ?: "Time not available"
            binding.radioButton.isChecked = task.isCompleted

            // Prevent triggering the listener when setting initial state
            binding.radioButton.setOnCheckedChangeListener(null)

            // Toggle completion state
            binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
                completionListener.onTaskCompletionToggled(task.firebaseTaskId, isChecked)
            }

            binding.root.setOnClickListener {
                itemClickListener.onItemClick(task)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Tasks>() {
        override fun areItemsTheSame(oldItem: Tasks, newItem: Tasks): Boolean {
            return oldItem.firebaseTaskId == newItem.firebaseTaskId
        }

        override fun areContentsTheSame(oldItem: Tasks, newItem: Tasks): Boolean {
            return oldItem == newItem
        }
    }

    interface TaskCompletionListener {
        fun onTaskCompletionToggled(firebaseTaskId: String, isCompleted: Boolean)
    }

    interface OnItemClickListener {
        fun onItemClick(task: Tasks)
    }
}
