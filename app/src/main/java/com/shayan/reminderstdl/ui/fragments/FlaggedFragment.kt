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
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.FragmentFlaggedBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel

class FlaggedFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {
    private var _binding: FragmentFlaggedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var flaggedAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlaggedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigate(R.id.flaggedFragment_to_homeFragment)
        }

        binding.flaggedRecycler.layoutManager = LinearLayoutManager(requireContext())
        flaggedAdapter = TaskAdapter(this, this)
        binding.flaggedRecycler.adapter = flaggedAdapter

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchFlaggedTasks()
        viewModel.flaggedTasks.observe(viewLifecycleOwner) { flaggedTasks ->
            flaggedAdapter.submitList(flaggedTasks)
            binding.flaggedRecycler.visibility =
                if (flaggedTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
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

    override fun onItemClick(task: Tasks) {
        // Navigate to Task Details Fragment
        val bundle = Bundle().apply {
            putParcelable("task", task)
        }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
