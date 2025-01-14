package com.shayan.reminderstdl.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.data.models.User
import com.shayan.reminderstdl.databinding.FragmentRegisterBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModel by activityViewModels()

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
            requireActivity().onBackPressed()
        }

        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()

            if (validateInput(email, password, confirmPassword, firstName, lastName)) {
                val user = User(
                    email = email, firstName = firstName, lastName = lastName
                )
                registerUser(user, password)
            }
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ): Boolean {
        return when {
            email.isEmpty() -> {
                showError("Email cannot be empty")
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

    private fun registerUser(user: User, password: String) {
        viewModel.register(user, password, onSuccess = {
            saveUserEmailToPrefs(requireContext(), user.email)

            Snackbar.make(requireView(), "Registration successful!", Snackbar.LENGTH_SHORT).show()
            findNavController().navigate(R.id.registerFragment_to_loginFragment)
        }, onError = { errorMessage ->
            Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show()
        })
    }

    private fun saveUserEmailToPrefs(context: Context, email: String) {
        val sharedPreferences = context.getSharedPreferences("PrefsDatabase", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("userEmail", email).apply()
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
