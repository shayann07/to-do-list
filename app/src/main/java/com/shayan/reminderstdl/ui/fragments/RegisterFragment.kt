package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.data.models.User
import com.shayan.reminderstdl.databinding.FragmentRegisterBinding
import com.shayan.reminderstdl.ui.viewmodels.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backOption.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.loginScreen.setOnClickListener {
            findNavController().navigate(R.id.registerFragment_to_loginFragment)
        }

        binding.signupButton.setOnClickListener {
            val phone = binding.phoneEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()

            if (validateInput(phone, password, confirmPassword, firstName, lastName)) {
                val user =
                    User(phone = phone, password = password, firstName = firstName, lastName = lastName)
                registerUser(user)
            }
        }

    }

    private fun validateInput(
        phone: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ): Boolean {
        return when {
            phone.isEmpty() -> {
                showError("Phone number cannot be empty")
                false
            }

            password.isEmpty() -> {
                showError("Password cannot be empty")
                false
            }

            confirmPassword.isEmpty() -> {
                showError("Confirm password cannot be empty")
                false
            }

            password != confirmPassword -> {
                showError("Passwords do not match")
                false
            }

            firstName.isEmpty() -> {
                showError("First name cannot be empty")
                false
            }

            lastName.isEmpty() -> {
                showError("Last name cannot be empty")
                false
            }

            else -> true
        }
    }

    private fun registerUser(user: User) {
        authViewModel.register(user, onSuccess = {
            Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.registerFragment_to_loginFragment)
        }, onError = { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        })
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
