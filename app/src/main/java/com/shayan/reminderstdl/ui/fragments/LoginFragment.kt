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

        binding.signupScreen.setOnClickListener {
            findNavController().navigate(R.id.loginFragment_to_registerFragment)
        }

        binding.loginButton.setOnClickListener {
            val phone = binding.phoneEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(phone, password)) {
                authViewModel.login(phone, password, onSuccess = { user ->
                    Toast.makeText(
                        requireContext(), "Welcome ${user.firstName}!", Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to HomeFragment and clear back stack
                    findNavController().navigate(
                        R.id.loginFragment_to_homeFragment, null, NavOptions.Builder().setPopUpTo(
                            R.id.nav_graph, true
                        ) // Clears all fragments up to the root of the navigation graph
                            .build()
                    )
                }, onError = { errorMessage ->
                    showError(errorMessage)
                })
            }
        }

    }

    private fun validateInput(phone: String, password: String): Boolean {
        return when {
            phone.isEmpty() -> {
                showError("Please enter a valid phone number")
                false
            }

            password.isEmpty() -> {
                showError("Please enter your password")
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
