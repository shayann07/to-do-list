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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        initializeObservers()
    }

    /**
     * Initialize UI components and set up event listeners.
     */
    private fun initializeComponents() {
        uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrEmpty()) {
            showSnackbar("User not logged in")
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.cancelButton.setOnClickListener { navigateToHome() }
        binding.addTaskButton.setOnClickListener { handleAddTask() }
        setupDateSwitch()
        setupTimeSwitch()
        setupFlagSwitch()
        setupLocationSwitch()
        setupLocationIconClick()
    }

    /**
     * Observe LiveData from the ViewModel.
     */
    private fun initializeObservers() {
        viewModel.taskCreationStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                showSnackbar("Task created successfully")
                clearForm()
                navigateToHome()
            } else {
                showSnackbar("Failed to create task. Try again.")
            }
        }
    }

    private fun setupDateSwitch() {
        binding.dateSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.calendarContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) selectedDate = null
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            binding.dateDisplay.text = selectedDate
            showSnackbar("Selected date: $selectedDate")
        }
    }

    private fun setupTimeSwitch() {
        binding.timeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showTimePicker() else clearTimeSelection()
        }
    }

    private fun determineTimeCategory(hour: Int): String {
        return when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..23, in 0..4 -> "tonight"
            else -> "unknown"
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.timeDisplay.text = selectedTime

                val timeCategory = determineTimeCategory(selectedHour)
                showSnackbar("Selected time: $selectedTime($timeCategory)")
            }, hour, minute, true
        ).show()
    }

    private fun setupFlagSwitch() {
        binding.flagSwitch.setOnCheckedChangeListener { _, isChecked ->
            isFlagged = isChecked
        }
    }

    private fun setupLocationSwitch() {
        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) handleLocationSwitch() else clearLocationSelection()
        }
    }

    private fun setupLocationIconClick() {
        binding.currentLocationIcon.setOnClickListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    openGoogleMaps(it.latitude, it.longitude)
                } ?: showSnackbar("Location not available")
            }
        }
    }

    private fun handleAddTask() {
        val title = binding.titleInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()

        if (title.isEmpty()) {
            showSnackbar("Title is required.")
            return
        }

        val task = Tasks(
            title = title,
            notes = notes,
            date = selectedDate,
            time = selectedTime,
            timeCategory = determineTimeCategory(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)),
            flag = isFlagged,
            location = if (binding.locationSwitch.isChecked) selectedLocation else null
        )

        viewModel.saveTask(uid!!, task)
    }

    private fun handleLocationSwitch() {
        if (isLocationPermissionGranted()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun getCurrentLocation() {
        if (isLocationEnabled()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    selectedLocation = "${it.latitude}, ${it.longitude}"
                    binding.currentLocationIcon.visibility = View.VISIBLE
                } ?: requestNewLocationData()
            }
        } else {
            showSnackbar("Please enable location services.")
        }
    }

    private fun requestNewLocationData() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let {
                            selectedLocation = "${it.latitude}, ${it.longitude}"
                            binding.currentLocationIcon.visibility = View.VISIBLE
                        }
                    }
                }, Looper.getMainLooper()
            )
        }
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun clearForm() {
        binding.titleInput.text.clear()
        binding.notesInput.text.clear()
        binding.dateDisplay.text = ""
        binding.timeDisplay.text = ""
        binding.flagSwitch.isChecked = false
        binding.locationSwitch.isChecked = false
        selectedDate = null
        selectedTime = null
        selectedLocation = null
    }

    private fun clearTimeSelection() {
        binding.timeDisplay.text = ""
        selectedTime = null
    }

    private fun clearLocationSelection() {
        binding.currentLocationIcon.visibility = View.GONE
        selectedLocation = null
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun openGoogleMaps(latitude: Double, longitude: Double) {
        val uri = Uri.parse("geo:$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            showSnackbar("Google Maps is not installed.")
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.newReminderFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
