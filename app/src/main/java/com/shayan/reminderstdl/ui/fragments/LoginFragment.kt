package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentLoginBinding
import com.shayan.reminderstdl.ui.viewmodels.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val phone = binding.phoneEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(phone, password)) {
                loginUser(phone, password)
            }
        }

        // Handle "Register" button click
        binding.signupScreen.setOnClickListener {
            findNavController().navigate(R.id.loginFragment_to_registerFragment)
        }
    }

    private fun loginUser(phone: String, password: String) {
        authViewModel.login(phone, password, onSuccess = { user ->
            Toast.makeText(
                requireContext(), "Welcome, ${user.firstName ?: "User"}!", Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(
                R.id.loginFragment_to_homeFragment, NavOptions.Builder().setPopUpTo(
                    R.id.nav_graph, true
                ).build()
            )
        }, onError = { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        })
    }

    private fun validateInput(phone: String, password: String): Boolean {
        return when {
            phone.isEmpty() -> {
                showError("Phone number cannot be empty")
                false
            }

            password.isEmpty() -> {
                showError("Password cannot be empty")
                false
            }

            else -> true
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
