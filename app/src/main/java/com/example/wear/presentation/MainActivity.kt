package com.example.wear.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import java.util.UUID


class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var devicesFound: MutableState<List<String>>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var isScanning: MutableState<Boolean>
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && result.isConnectable) {
                val deviceName = result.device.name ?: "Unknown Device"
                val deviceInfo = "Device Name: $deviceName\n" +
                        "Device Address: ${result.device.address}\n" +
                        "Signal Strength: ${result.rssi}dBm\n"
                devicesFound.value += deviceInfo
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        devicesFound = mutableStateOf(listOf())
        isScanning = mutableStateOf(false)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                startBleScan()
            } else {
                // Handle the logic for at least one denied permission
            }
        }

        setContent {
            WearApp(devicesFound, isScanning, ::startBleScan, ::stopBleScan, ::clearAndRescan)
        }

        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }

    private fun startBleScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val filters = listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb"))).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothAdapter.bluetoothLeScanner.startScan(filters, settings, scanCallback)
        isScanning.value = true
    }

    private fun stopBleScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // 權限未被授予，處理權限請求或SecurityException
            return
        }
        if (isScanning.value) {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
            isScanning.value = false
        }
    }


    private fun clearAndRescan() {
        devicesFound.value = listOf()
        startBleScan()
    }
}
