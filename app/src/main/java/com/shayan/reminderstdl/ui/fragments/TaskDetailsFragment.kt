package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.FragmentTaskDetailsBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel

class TaskDetailsFragment : Fragment() {

    private var _binding: FragmentTaskDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val task = arguments?.getParcelable<Tasks>("task")

        if (task != null) {
            binding.tvTitle.text = task.title
            if (!task.notes.isNullOrBlank()) {
                binding.tvNotes.text = task.notes
                binding.tvNotes.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.grey
                    )
                )
            } else {
                binding.tvNotes.text = binding.root.context.getString(R.string.notes_not_available)
                binding.tvNotes.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context, R.color.darker_gray
                    )
                )
            }
            binding.tvDate.text = task.date ?: "Not available"
            binding.tvTime.text = task.time ?: "Not available"
            binding.tvFlag.text = if (task.flag) "Yes" else "No"
            binding.tvCompleted.text = if (task.isCompleted) "Yes" else "No"
        }
    }
}