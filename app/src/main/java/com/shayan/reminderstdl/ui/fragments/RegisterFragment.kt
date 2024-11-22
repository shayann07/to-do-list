package com.shayan.reminderstdl.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentRegisterBinding
import com.shayan.reminderstdl.ui.viewmodels.AuthViewModel


class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding: FragmentRegisterBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        setClickListeners()
    }

    private fun setClickListeners() {
        binding.apply {
            // Navigate back to the previous fragment
            backOption.setOnClickListener { findNavController().navigateUp() }

            // Handle the signup button click
            signupButton.setOnClickListener {
                val firstName = firstName.text.toString().trim()
                val lastName = lastName.text.toString().trim()
                val phone = phoneEditText.text.toString().trim()
                val pass = passwordEditText.text.toString()
                val cPass = confirmPasswordEditText.text.toString()

                if (validateInput(firstName, lastName, phone, pass, cPass)) {
                    authViewModel.register(firstName, lastName, phone, pass, cPass, onSuccess = {
                        Toast.makeText(
                            requireContext(), "User registered successfully", Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.registerFragment_to_loginFragment)
                    }, onError = { errorMessage ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }

    private fun validateInput(
        firstName: String, lastName: String, phone: String, pass: String, cPass: String
    ): Boolean {
        return when {
            firstName.isEmpty() -> {
                showToast("First name cannot be empty")
                false
            }

            lastName.isEmpty() -> {
                showToast("Last name cannot be empty")
                false
            }

            phone.isEmpty() -> {
                showToast("Phone number cannot be empty")
                false
            }

            pass.isEmpty() || cPass.isEmpty() -> {
                showToast("Password fields cannot be empty")
                false
            }

            pass != cPass -> {
                showToast("Passwords do not match")
                false
            }

            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}


//class RegisterFragment : Fragment() {
//
//    private lateinit var binding: FragmentRegisterBinding
//    private val authViewModel: AuthViewModel by activityViewModels()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding = FragmentRegisterBinding.bind(view)
//
//        binding.apply {
//
//            backOption.setOnClickListener {
//                findNavController().navigateUp()
//            }
//
//            signupButton.setOnClickListener {
//                val firstName = firstName.text.toString()
//                val lastName = lastName.text.toString()
//                val phone = phoneEditText.text.toString()
//                val pass = passwordEditText.text.toString()
//                val cPass = confirmPasswordEditText.text.toString()
//
//
//                if (pass == cPass) {
//                    authViewModel.register(firstName, lastName, phone, pass, cPass, onSuccess = {
//                        Toast.makeText(
//                            requireContext(), "User registered successfully", Toast.LENGTH_SHORT
//                        ).show()
//                        findNavController().navigate(R.id.registerFragment_to_loginFragment)
//                    }, onError = { errorMessage ->
//                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
//                    })
//                } else {
//                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//        }
//    }
//}