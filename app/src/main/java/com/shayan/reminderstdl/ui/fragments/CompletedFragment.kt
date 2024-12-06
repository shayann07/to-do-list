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
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.adapters.TaskAdapter
import com.shayan.reminderstdl.databinding.FragmentCompletedBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel

class CompletedFragment : Fragment(), TaskAdapter.TaskCompletionListener {
    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var completedAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigate(R.id.completedFragment_to_homeFragment)
        }

        binding.completedRecycler.layoutManager = LinearLayoutManager(requireContext())
        completedAdapter = TaskAdapter(this)
        binding.completedRecycler.adapter = completedAdapter

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchCompletedTasks()
        viewModel.completedTasks.observe(viewLifecycleOwner) { completedTasks ->
            completedAdapter.submitList(completedTasks)
            binding.completedRecycler.visibility =
                if (completedTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onTaskCompletionToggled(
        firebaseTaskId: String, isCompleted: Boolean
    ) {
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
        _binding = null
    }
}
