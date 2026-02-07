package com.example.smssheettracker.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smssheettracker.data.remote.SecureCredentialStore
import com.example.smssheettracker.databinding.ActivityMainBinding
import com.example.smssheettracker.service.SmsForegroundService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialStore: SecureCredentialStore

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        startTrackerService()
    }

    private val serviceJsonPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) persistServiceJson(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialStore = SecureCredentialStore(this)

        binding.btnGrantPermissions.setOnClickListener { requestRuntimePermissions() }
        binding.btnPickServiceJson.setOnClickListener { serviceJsonPicker.launch("application/json") }
        binding.btnBatteryWhitelist.setOnClickListener { requestIgnoreBatteryOptimization() }
        binding.btnStartService.setOnClickListener { startTrackerService() }
    }

    private fun requestRuntimePermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        smsPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startTrackerService() {
        ContextCompat.startForegroundService(this, Intent(this, SmsForegroundService::class.java))
        Toast.makeText(this, "Foreground tracker started", Toast.LENGTH_SHORT).show()
    }

    private fun persistServiceJson(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { stream ->
            val json = stream.bufferedReader().readText()
            credentialStore.saveServiceAccountJson(json)
            Toast.makeText(this, "Service account JSON securely saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestIgnoreBatteryOptimization() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}
