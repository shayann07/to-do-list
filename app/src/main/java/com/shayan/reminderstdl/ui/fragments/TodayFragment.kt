package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.adapters.TaskAdapter
import com.shayan.reminderstdl.databinding.FragmentTodayBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel

class TodayFragment : Fragment(), TaskAdapter.TaskCompletionListener {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var morningAdapter: TaskAdapter
    private lateinit var afternoonAdapter: TaskAdapter
    private lateinit var tonightAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigate(R.id.todayFragment_to_homeFragment)
        }
        binding.newReminderButton.setOnClickListener {
            findNavController().navigate(R.id.todayFragment_to_newReminderFragment)
        }

        // Initialize RecyclerViews
        binding.recyclerMorning.layoutManager = LinearLayoutManager(context)
        binding.recyclerAfternoon.layoutManager = LinearLayoutManager(context)
        binding.recyclerTonight.layoutManager = LinearLayoutManager(context)

        // Initialize Adapters
        morningAdapter = TaskAdapter(this)
        afternoonAdapter = TaskAdapter(this)
        tonightAdapter = TaskAdapter(this)

        binding.recyclerMorning.adapter = morningAdapter
        binding.recyclerAfternoon.adapter = afternoonAdapter
        binding.recyclerTonight.adapter = tonightAdapter

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        // Fetch tasks
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.fetchTasks(userId)

            viewModel.morningTasksLiveData.observe(viewLifecycleOwner) { morningTasks ->
                if (morningTasks.isNullOrEmpty()) {
                    binding.recyclerMorning.visibility = View.GONE
                } else {
                    binding.recyclerMorning.visibility = View.VISIBLE
                    morningAdapter.submitList(morningTasks)
                }
            }

            viewModel.afternoonTasksLiveData.observe(viewLifecycleOwner) { afternoonTasks ->
                if (afternoonTasks.isNullOrEmpty()) {
                    binding.recyclerAfternoon.visibility = View.GONE
                } else {
                    binding.recyclerAfternoon.visibility = View.VISIBLE
                    afternoonAdapter.submitList(afternoonTasks)
                }
            }

            viewModel.tonightTasksLiveData.observe(viewLifecycleOwner) { tonightTasks ->
                if (tonightTasks.isNullOrEmpty()) {
                    binding.recyclerTonight.visibility = View.GONE
                } else {
                    binding.recyclerTonight.visibility = View.VISIBLE
                    tonightAdapter.submitList(tonightTasks)
                }
            }
        }
    }

    override fun onTaskCompletionToggled(firebaseTaskId: String, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(firebaseTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to avoid memory leaks
    }
}

