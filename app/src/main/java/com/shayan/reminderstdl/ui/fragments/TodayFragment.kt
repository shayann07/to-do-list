package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.adapters.TaskAdapter
import com.shayan.reminderstdl.databinding.FragmentTodayBinding
import com.shayan.reminderstdl.ui.viewmodels.ViewModel

class TodayFragment : Fragment() {
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

        // Initialize RecyclerViews
        binding.recyclerMorning.layoutManager = LinearLayoutManager(context)
        binding.recyclerAfternoon.layoutManager = LinearLayoutManager(context)
        binding.recyclerTonight.layoutManager = LinearLayoutManager(context)

        // Initialize Adapters
        morningAdapter = TaskAdapter()
        afternoonAdapter = TaskAdapter()
        tonightAdapter = TaskAdapter()

        binding.recyclerMorning.adapter = morningAdapter
        binding.recyclerAfternoon.adapter = afternoonAdapter
        binding.recyclerTonight.adapter = tonightAdapter

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        // Fetch tasks
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.fetchTasks(userId)

            viewModel.tasksList.observe(viewLifecycleOwner) { tasks ->
                tasks?.let {
                    val morningTasks = it.filter { task -> task.timeCategory == "morning" }
                    val afternoonTasks = it.filter { task -> task.timeCategory == "afternoon" }
                    val tonightTasks = it.filter { task -> task.timeCategory == "tonight" }

                    morningAdapter.submitList(morningTasks)
                    afternoonAdapter.submitList(afternoonTasks)
                    tonightAdapter.submitList(tonightTasks)

                    // Toggle visibility
                    binding.recyclerMorning.visibility =
                        if (morningTasks.isEmpty()) View.GONE else View.VISIBLE
                    binding.recyclerAfternoon.visibility =
                        if (afternoonTasks.isEmpty()) View.GONE else View.VISIBLE
                    binding.recyclerTonight.visibility =
                        if (tonightTasks.isEmpty()) View.GONE else View.VISIBLE
                }
            }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to avoid memory leaks
    }
}
