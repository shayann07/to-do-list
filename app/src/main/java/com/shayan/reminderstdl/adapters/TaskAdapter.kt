package com.shayan.reminderstdl.adapters

import android.view.LayoutInflater
import android.view.View
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
        val isLastItem = position == currentList.size - 1
        holder.bind(task, completionListener, itemClickListener, deleteClickListener, isLastItem)
    }

    class TaskViewHolder(private val binding: ReminderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            task: Tasks,
            completionListener: TaskCompletionListener,
            itemClickListener: OnItemClickListener,
            deleteClickListener: OnDeleteClickListener,
            isLastItem: Boolean
        ) {
            binding.fetchedTaskTitle.text = task.title

            // Set time and color based on availability
            if (!task.time.isNullOrBlank()) {
                binding.fetchedTaskTime.text = task.time
                binding.fetchedTaskTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.light_blue)
                )
            } else {
                binding.fetchedTaskTime.text =
                    binding.root.context.getString(R.string.time_not_available)
                binding.fetchedTaskTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.orange)
                )
            }

            // Update completion state
            binding.radioButton.isChecked = task.isCompleted
            binding.radioButton.setOnCheckedChangeListener(null) // Prevent triggering listener
            binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
                completionListener.onTaskCompletionToggled(task.firebaseTaskId, isChecked)
            }

            // Divider visibility for the last item
            binding.recyclerViewDivider.visibility = if (isLastItem) View.GONE else View.VISIBLE

            // Delete and item click listeners
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
