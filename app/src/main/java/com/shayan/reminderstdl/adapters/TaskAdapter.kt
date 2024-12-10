package com.shayan.reminderstdl.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.ReminderItemsBinding

class TaskAdapter(
    private val completionListener: TaskCompletionListener,
    private val itemClickListener: OnItemClickListener,
    private val deleteClickListener: OnDeleteClickListener
) : ListAdapter<Tasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            ReminderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task, completionListener, itemClickListener, deleteClickListener)
    }

    class TaskViewHolder(private val binding: ReminderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            task: Tasks,
            completionListener: TaskCompletionListener,
            itemClickListener: OnItemClickListener,
            deleteClickListener: OnDeleteClickListener
        ) {
            binding.fetchedTaskTitle.text = task.title

            if (!task.time.isNullOrBlank()) {
                binding.fetchedTaskTime.text = task.time
                binding.fetchedTaskTime.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.light_blue
                    )
                )
            } else {
                binding.fetchedTaskTime.text =
                    binding.root.context.getString(R.string.time_not_available)
                binding.fetchedTaskTime.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.orange
                    )
                )
            }


            binding.radioButton.isChecked = task.isCompleted

            // Prevent triggering the listener when setting initial state
            binding.radioButton.setOnCheckedChangeListener(null)

            // Toggle completion state
            binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
                completionListener.onTaskCompletionToggled(task.firebaseTaskId, isChecked)
            }
            binding.deleteTask.setOnClickListener {
                deleteClickListener.onDeleteClick(task)
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

    interface OnDeleteClickListener {
        fun onDeleteClick(task: Tasks)
    }
}
