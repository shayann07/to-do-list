package com.shayan.reminderstdl.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentLoginBinding
import com.shayan.reminderstdl.ui.viewmodel.ViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModel by activityViewModels()

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
            val phone = binding.emailEditText.text.toString().trim()
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

    private fun loginUser(email: String, password: String) {
        viewModel.login(email, password, onSuccess = { user ->
            saveUserEmailToPrefs(requireContext(), email)

            Snackbar.make(
                requireView(), "Welcome, ${user.firstName ?: "User"}!", Snackbar.LENGTH_SHORT
            ).show()

            val navOptions = NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
            findNavController().navigate(R.id.loginFragment_to_homeFragment, null, navOptions)
        }, onError = { errorMessage ->
            Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_SHORT).show()
        })
    }

    private fun saveUserEmailToPrefs(context: Context, email: String) {
        val sharedPreferences = context.getSharedPreferences("PrefsDatabase", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("userEmail", email).apply()
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
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
