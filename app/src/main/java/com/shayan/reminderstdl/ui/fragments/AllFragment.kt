package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.adapters.TaskAdapter
import com.shayan.reminderstdl.databinding.FragmentAllBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel

class AllFragment : Fragment(), TaskAdapter.TaskCompletionListener {

    private var _binding: FragmentAllBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ViewModel
    private lateinit var allAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigate(R.id.allFragment_to_homeFragment)
        }

        binding.allRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        allAdapter = TaskAdapter(this)

        binding.allRecyclerView.adapter = allAdapter

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        viewModel.incompleteTasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNullOrEmpty()) {
                binding.allRecyclerView.visibility = View.GONE
            } else {
                binding.allRecyclerView.visibility = View.VISIBLE
                allAdapter.submitList(tasks)
            }
        }


    }

    override fun onTaskCompletionToggled(taskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(taskId, isCompleted)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}