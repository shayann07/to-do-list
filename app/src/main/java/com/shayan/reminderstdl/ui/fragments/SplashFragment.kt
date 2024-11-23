package com.shayan.reminderstdl.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        context?.deleteDatabase("app_database")

        sharedPreferences =
            requireContext().getSharedPreferences("PrefsDatabase", Context.MODE_PRIVATE)

        // Check if the user is logged in
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoggedIn) {
                // If the user is logged in, navigate directly to the home screen
                findNavController().navigate(R.id.splashFragment_to_homeFragment)
            } else {
                // If not, navigate to the login screen
                findNavController().navigate(R.id.splashFragment_to_loginFragment)
            }
        }, 5650) // You can adjust the delay time
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
