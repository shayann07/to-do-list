package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentHomeBinding
import com.shayan.reminderstdl.ui.viewmodels.ViewModel
import kotlin.reflect.KMutableProperty0

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private var isArrowDownICloud = true
    private var isArrowDownOutlook = true
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using View Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        viewModel.todayTaskCount.observe(viewLifecycleOwner) { count ->
            binding.todayCount.text = "$count"
        }

        binding.todayScreen.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_todayFragment)
        }

        // Set up menu for log-out functionality
        binding.menuImageView.setOnClickListener { showPopupMenu() }

        // Toggle visibility for containers on click
        binding.textviewICloud.setOnClickListener {
            toggleVisibility(binding.iCloudContainer, ::isArrowDownICloud)
        }
        binding.textviewOutlook.setOnClickListener {
            toggleVisibility(binding.outlookContainer, ::isArrowDownOutlook)
        }

        binding.newReminderButton.setOnClickListener {
            findNavController().navigate(R.id.homeFragment_to_newReminderFragment)
        }
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
                // Sign out the user using Firebase Authentication
                firebaseAuth.signOut()

                // Show Snackbar after logging out
                Snackbar.make(binding.root, "Successfully logged out", Snackbar.LENGTH_SHORT).show()

                // Navigate back to the login screen
                findNavController().navigate(R.id.homeFragment_to_loginFragment)
                true
            }

            else -> false
        }
    }

    private fun toggleVisibility(container: LinearLayout, arrowState: KMutableProperty0<Boolean>) {
        container.visibility = if (arrowState.get()) View.GONE else View.VISIBLE
        arrowState.set(!arrowState.get())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
