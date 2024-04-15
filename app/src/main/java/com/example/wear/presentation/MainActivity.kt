package com.example.wear.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var devicesFound: MutableState<List<String>>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var isScanning: MutableState<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        devicesFound = mutableStateOf(listOf())
        // 初始化ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                // 所有權限被授予，可以進行BLE掃描
                startBleScan()
            } else {
                // 至少有一個權限被拒絕，處理您的邏輯
            }
        }
        isScanning = mutableStateOf(false)

        setContent {
            WearApp(devicesFound, isScanning)
        }
        // 請求權限
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }
    private fun startBleScan() {
        // 檢查藍牙掃描和連接權限是否已被授予
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 如果沒有權限，處理權限請求或SecurityException
            return
        }
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                // 檢查是否有權限訪問藍牙裝置名稱
                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val deviceName = result.device.name ?: "Unknown Device"
                    val deviceInfo = "Device Name: $deviceName\n" +
                            "Device Address: ${result.device.address}\n" +
                            "Signal Strength: ${result.rssi}dBm\n"
                    devicesFound.value += deviceInfo
                } else {
                    // 沒有權限，可以在這裡處理或記錄
                }
            }
        }
        isScanning.value = true
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }
}
