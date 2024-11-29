package com.shayan.reminderstdl.ui.fragments

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.shayan.reminderstdl.R
import com.shayan.reminderstdl.data.models.Tasks
import com.shayan.reminderstdl.databinding.FragmentNewReminderBinding
import com.shayan.reminderstdl.ui.viewmodels.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class NewReminderFragment : Fragment() {

    private var _binding: FragmentNewReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var isFlagged: Boolean = false
    private var selectedLocation: String? = null
    private var uid: String? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                binding.currentLocationIcon.visibility = View.VISIBLE
                selectedLocation = "${location.latitude}, ${location.longitude}"
                fusedLocationClient.removeLocationUpdates(this)
            } else {
                Toast.makeText(
                    requireContext(), "Unable to fetch new location data", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelButton.setOnClickListener {
            findNavController().navigate(R.id.newReminderFragment_to_homeFragment)
        }

        // Retrieve the UID from FirebaseAuth
        uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Snackbar.make(binding.root, "User not logged in", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Enable the "addTaskButton" after successful UID fetch
        binding.addTaskButton.setOnClickListener {
            val title = binding.titleInput.text.toString().trim()
            val notes = binding.notesInput.text.toString().trim()

            if (title.isNotEmpty()) {
                val task = Tasks(
                    title = title,
                    notes = notes,
                    date = selectedDate,
                    time = selectedTime,
                    flag = isFlagged,
                    location = if (binding.locationSwitch.isChecked) selectedLocation else null
                )
                // Save the task under the specific user's UID
                viewModel.saveTask(uid!!, task)
            } else {
                Snackbar.make(binding.root, "At least, enter a title", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Setup other views and functionality
        setupDateSwitch()
        setupTimeSwitch()
        setupFlagSwitch()
        setupLocationSwitch()
        setupLocationIconClick()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Observe task creation status
        viewModel.taskCreationStatus.observe(viewLifecycleOwner, { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Task created successfully", Toast.LENGTH_SHORT)
                    .show()
                clearForm()
                findNavController().navigate(R.id.newReminderFragment_to_homeFragment)
            } else {
                Toast.makeText(requireContext(), "Task creation failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDateSwitch() {
        binding.dateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.calendarContainer.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Date picker visible", Toast.LENGTH_SHORT).show()
            } else {
                binding.calendarContainer.visibility = View.GONE
                selectedDate = null
                Toast.makeText(requireContext(), "Date selection cleared", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
// Update UI and show Toast with the selected date
            binding.dateDisplay.text = selectedDate
            Toast.makeText(requireContext(), "Selected date: $selectedDate", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setupTimeSwitch() {
        binding.timeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePicker()
            } else {
                binding.timeDisplay.text = ""
                selectedTime = null
                Toast.makeText(requireContext(), "Time selection cleared", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.timeDisplay.text = selectedTime
                Toast.makeText(requireContext(), "Selected time: $selectedTime", Toast.LENGTH_SHORT)
                    .show()
            }, hour, minute, true // 24-hour format
        )
        timePickerDialog.show()
    }

    private fun setupFlagSwitch() {
        binding.flagSwitch.setOnCheckedChangeListener { _, isChecked ->
            isFlagged = isChecked
            Toast.makeText(
                requireContext(),
                if (isChecked) "Reminder flagged" else "Reminder unflagged",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupLocationSwitch() {
        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handleLocationSwitch()
            } else {
                binding.currentLocationIcon.visibility = View.GONE
                selectedLocation = null
                Toast.makeText(requireContext(), "Location cleared", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLocationSwitch() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation() {
        try {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        binding.currentLocationIcon.visibility = View.VISIBLE
                        selectedLocation = "${location.latitude}, ${location.longitude}"
                    } else {
                        requestNewLocationData()
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(), "Failed to retrieve location", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "Enable location services", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true).build()

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun setupLocationIconClick() {
        binding.currentLocationIcon.setOnClickListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val uri = Uri.parse("geo:${location.latitude},${location.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            requireContext(), "Google Maps not installed", Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Helper function to clear the task input form
    private fun clearForm() {
        binding.titleInput.text.clear()
        binding.notesInput.text.clear()
        selectedDate = null
        selectedTime = null
        isFlagged = false
        selectedLocation = null
        binding.locationSwitch.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}