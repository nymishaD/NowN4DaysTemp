package com.example.nown4daystemp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nown4daystemp.databinding.ActivityMainBinding
import com.example.nown4daystemp.databinding.LayoutNextDayBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private lateinit var layouts : List<LayoutNextDayBinding>
    private lateinit var cityName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        layouts = listOf(
            binding.LayoutDay1,
            binding.LayoutDay2,
            binding.LayoutDay3,
            binding.LayoutDay4
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()
    }

    private fun observeViewModel() {
        viewModel.currentWeather.observe(this) { weather ->
            updateCurrentWeatherUI(weather)
        }

        viewModel.averageTemperatures.observe(this) { averageTemp ->
            updateNextDayForecastUI(averageTemp)
        }

        viewModel.errorOccurred.observe(this) { errorOccurred ->
            if (errorOccurred) {
                showSnackBarWithButton(
                    findViewById(android.R.id.content),
                    getString(R.string.something_went_wrong),
                    getString(R.string.retry)
                ) {
                    viewModel.fetchWeatherResults(cityName)
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if(isLoading) {
                showProgressBar()
            } else {
                hideProgressBar()
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSnackBarWithButton(view: View, message: String, buttonText: String, buttonClickListener: View.OnClickListener) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)

        val buttonSpannable = SpannableString(buttonText)
        buttonSpannable.setSpan(
            ForegroundColorSpan(Color.RED),
            0, buttonText.length, 0
        )
        snackBar.setAction(buttonSpannable) { buttonClickListener.onClick(view) }

        snackBar.show()
    }

    private fun updateNextDayForecastUI(averageTemp: Map<String, Int>) {
        val nextFourDays = averageTemp.entries.drop(1).take(4)

        for ((index, entry) in nextFourDays.withIndex()) {
            val (day, temperatures) = entry
            layouts[index].day.text = day
            val t = "$temperatures C"
            layouts[index].temp.text = t
        }
    }

    private fun updateCurrentWeatherUI(weather: Map<String, Int>) {
        val temp = weather.values.firstOrNull()
        binding.cityName.text = weather.keys.firstOrNull()
        val t = "$temp\u00B0"
        binding.todayTemp.text = t
    }

    private fun getLocation() {
        checkPermission()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(applicationContext, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        if (addresses?.isNotEmpty() == true) {
                            cityName = addresses[0].locality ?: ""
                            viewModel.fetchWeatherResults(cityName)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCoarsePermission()
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestFinePermission()
        }
    }

    private fun requestCoarsePermission() {
        startCoarseLocationPermissionRequest()
    }

    private fun requestFinePermission() {
        startFineLocationPermissionRequest()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocation()
        } else {
            Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_LONG).show()
        }
    }

    private fun startCoarseLocationPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun startFineLocationPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}