package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentHomeBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel
import kotlin.reflect.KMutableProperty0

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var firebaseAuth: FirebaseAuth

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
        fetchUserTasks()
        observeTaskCounts()
        setupClickListeners()
    }

    private fun setupFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }

    private fun fetchUserTasks() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            viewModel.fetchTasks(userId)
        } else {
            Toast.makeText(requireContext(), "No tasks to fetch", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeTaskCounts() {
        with(viewModel) {
            todayTaskCount.observe(viewLifecycleOwner) { count ->
                binding.todayCount.text = count.toString()
            }
            scheduledTasksCount.observe(viewLifecycleOwner) { count ->
                binding.scheduledCount.text = count.toString()
            }
            flaggedTasksCount.observe(viewLifecycleOwner) { count ->
                binding.flaggedCount.text = count.toString()
            }
            incompleteTasksCount.observe(viewLifecycleOwner) { count ->
                binding.allCount.text = count.toString()
                binding.outlookCount.text = count.toString()
            }
            totalTaskCount.observe(viewLifecycleOwner) { count ->
                binding.iCloudCount.text = count.toString()
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Navigation to different fragments
            todayScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_todayFragment) }
            scheduledScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_scheduledFragment) }
            allScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_allFragment) }
            flaggedScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_flaggedFragment) }
            completedScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_completedFragment) }
            iCloudContainer.setOnClickListener { navigateTo(R.id.homeFragment_to_iCloudFragment) }
            outlookContainer.setOnClickListener { navigateTo(R.id.homeFragment_to_outlookFragment) }

            // Log-out menu
            menuImageView.setOnClickListener { showPopupMenu() }

            // Toggling visibility of containers
            textviewICloud.setOnClickListener {
                toggleVisibility(iCloudContainer, ::isArrowDownICloud)
            }
            textviewOutlook.setOnClickListener {
                toggleVisibility(outlookContainer, ::isArrowDownOutlook)
            }

            // New Reminder button
            newReminderButton.setOnClickListener {
                navigateTo(R.id.homeFragment_to_newReminderFragment)
            }
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
                firebaseAuth.signOut()
                viewModel.clearAllTasks()
                Snackbar.make(binding.root, "Successfully logged out", Snackbar.LENGTH_SHORT).show()
                navigateTo(R.id.homeFragment_to_loginFragment)
                true
            }

            else -> false
        }
    }

    private fun toggleVisibility(container: LinearLayout, arrowState: KMutableProperty0<Boolean>) {
        container.visibility = if (arrowState.get()) View.GONE else View.VISIBLE
        arrowState.set(!arrowState.get())
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
            fetchTotalTasks()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
