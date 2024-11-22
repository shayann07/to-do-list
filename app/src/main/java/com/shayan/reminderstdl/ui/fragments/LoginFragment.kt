package com.shayan.reminderstdl.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentLoginBinding
import com.shayan.reminderstdl.ui.viewmodels.AuthViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var authViewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLoginBinding.bind(view)
        sharedPreferences =
            requireActivity().getSharedPreferences("PrefsDatabase", Context.MODE_PRIVATE)

        // Initialize the ViewModel manually with SharedPreferences and AuthRepository
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        binding.apply {
            signupScreen.setOnClickListener {
                findNavController().navigate(R.id.loginFragment_to_registerFragment)
            }

            loginButton.setOnClickListener {
                val phone = phoneEditText.text.toString().trim()
                val pass = passwordEditText.text.toString().trim()

                if (phone.isEmpty()) {
                    Toast.makeText(
                        requireContext(), "Please enter your phone number", Toast.LENGTH_SHORT
                    ).show()
                } else if (pass.isEmpty()) {
                    Toast.makeText(
                        requireContext(), "Please enter your password", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    authViewModel.login(phone, pass, onSuccess = { user ->
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().navigate(R.id.loginFragment_to_homeFragment)
                    }, onError = { errorMessage ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }
}
