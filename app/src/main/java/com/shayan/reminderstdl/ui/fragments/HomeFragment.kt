package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.adapters.TaskAdapter
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.FragmentHomeBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel
import kotlin.reflect.KMutableProperty0

class HomeFragment : Fragment() {

    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel and Firebase
    private lateinit var viewModel: ViewModel
    private lateinit var firebaseAuth: FirebaseAuth

    // Adapter and Task Data
    private lateinit var taskAdapter: TaskAdapter

    // State for container visibility
    private var isArrowDownICloud = true
    private var isArrowDownOutlook = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFirebaseAuth()
        initializeViewModel()
        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupClickListeners()
        fetchUserTasks()
    }

    private fun setupFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(completionListener = object : TaskAdapter.TaskCompletionListener {
            override fun onTaskCompletionToggled(firebaseTaskId: String, isCompleted: Boolean) {
                viewModel.toggleTaskCompletion(
                    firebaseTaskId, isCompleted
                ) { success, message ->
                    Toast.makeText(
                        requireContext(),
                        if (success) "Task updated" else "Failed to update: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                    val currentQuery = binding.searchView.query.toString()
                    if (currentQuery.isNotEmpty()) {
                        viewModel.fetchTasksByTitle(currentQuery) // Update the UI
                    }
                }
            }
        }, deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
            override fun onDeleteClick(task: Tasks) {
                viewModel.deleteTask(task.firebaseTaskId)
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()

            }
        }, itemClickListener = object : TaskAdapter.OnItemClickListener {
            override fun onItemClick(task: Tasks) {
                val bundle = Bundle().apply {
                    putParcelable("task", task) // Pass the task object
                }
                findNavController().navigate(R.id.taskDetailsFragment, bundle)
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
            visibility = View.GONE
        }
    }


    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    taskAdapter.submitList(emptyList()) // Clear the adapter
                    binding.recyclerView.visibility = View.GONE // Hide RecyclerView
                    binding.buttonContainer.visibility = View.VISIBLE // Show new_reminder_button
                    binding.gridLayout.visibility = View.VISIBLE // Show grid_layout
                    binding.homeComponent.visibility = View.VISIBLE // Show homeComponent2
                } else {
                    viewModel.fetchTasksByTitle(newText)
                    binding.recyclerView.visibility = View.VISIBLE // Show RecyclerView
                    binding.buttonContainer.visibility = View.GONE // Hide new_reminder_button
                    binding.gridLayout.visibility = View.GONE // Hide grid_layout
                    binding.homeComponent.visibility = View.GONE // Hide homeComponent2
                }
                return true
            }
        })

        // Make RecyclerView visible when the search view container is clicked
        binding.searchViewContainer.setOnClickListener {
            binding.recyclerView.visibility = View.VISIBLE
            binding.searchView.requestFocus() // Activate SearchView
            binding.buttonContainer.visibility = View.GONE // Hide new_reminder_button
            binding.gridLayout.visibility = View.GONE // Hide grid_layout
            binding.homeComponent.visibility = View.GONE // Hide homeComponent2
        }
    }

    private fun setupObservers() {
        viewModel.searchQueryResult.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNotEmpty()) {
                binding.recyclerView.visibility = View.VISIBLE
                taskAdapter.submitList(tasks)
            } else {
                binding.recyclerView.visibility = View.GONE
                Toast.makeText(requireContext(), "No tasks found", Toast.LENGTH_SHORT).show()
            }
        }

        with(viewModel) {
            todayTaskCount.observe(viewLifecycleOwner) { updateTaskCount(binding.todayCount, it) }
            scheduledTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(
                    binding.scheduledCount, it
                )
            }
            flaggedTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(
                    binding.flaggedCount, it
                )
            }
            incompleteTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(
                    binding.allCount, it
                )
            }
            totalTaskCount.observe(viewLifecycleOwner) { updateTaskCount(binding.iCloudCount, it) }
        }
    }

    private fun updateTaskCount(textView: TextView, count: Int) {
        textView.text = count.toString()
    }

    private fun setupClickListeners() {
        with(binding) {
            // Navigation
            todayScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_todayFragment) }
            scheduledScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_scheduledFragment) }
            allScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_allFragment) }
            flaggedScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_flaggedFragment) }
            completedScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_completedFragment) }
            iCloudContainer.setOnClickListener { navigateTo(R.id.homeFragment_to_iCloudFragment) }
            outlookContainer.setOnClickListener { navigateTo(R.id.homeFragment_to_outlookFragment) }

            // Log-out menu
            menuImageView.setOnClickListener { showPopupMenu() }

            // Toggle visibility
            textviewICloud.setOnClickListener {
                toggleVisibility(
                    iCloudContainer, ::isArrowDownICloud
                )
            }
            textviewOutlook.setOnClickListener {
                toggleVisibility(
                    outlookContainer, ::isArrowDownOutlook
                )
            }

            // Create new reminder
            newReminderButton.setOnClickListener { navigateTo(R.id.homeFragment_to_newReminderFragment) }
        }
    }

    private fun navigateTo(actionId: Int) {
        findNavController().navigate(actionId)
    }

    private fun showPopupMenu() {
        PopupMenu(requireContext(), binding.menuImageView).apply {
            menuInflater.inflate(R.menu.menu_dropdown_toolbar, menu)
            setOnMenuItemClickListener { handleMenuItemClick(it) }
            show()
        }
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                performLogout()
                true
            }

            else -> false
        }
    }

    private fun performLogout() {
        firebaseAuth.signOut()
        viewModel.clearAllTasks()
        Snackbar.make(binding.root, "Successfully logged out", Snackbar.LENGTH_SHORT).show()
        navigateTo(R.id.homeFragment_to_loginFragment)
    }

    private fun toggleVisibility(container: LinearLayout, arrowState: KMutableProperty0<Boolean>) {
        container.visibility = if (arrowState.get()) View.GONE else View.VISIBLE
        arrowState.set(!arrowState.get())
    }

    private fun fetchUserTasks() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            viewModel.fetchTasks(userId)
        } else {
            Toast.makeText(requireContext(), "No tasks to fetch", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTaskCounts()
    }

    private fun refreshTaskCounts() {
        with(viewModel) {
            fetchTodayTasks()
            fetchScheduledTasks()
            fetchIncompleteTasks()
            fetchCompletedTasks()
            fetchFlaggedTasks()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
