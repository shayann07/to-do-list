package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentFlaggedBinding

class FlaggedFragment : Fragment() {
    private var _binding: FragmentFlaggedBinding? = null
    private val binding get() = _binding!!

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
    }
}
