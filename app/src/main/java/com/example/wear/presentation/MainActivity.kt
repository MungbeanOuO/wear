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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text // 保留這個導入，移除 Wear OS 的 Text 導入
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.TimeText // 保留 TimeText 的導入，如果您在 Wear OS 應用中需要它
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.presentation.theme.WearTheme

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


@Composable
fun WearApp(devicesFound: MutableState<List<String>>, isScanning: MutableState<Boolean>) {
    WearTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            ScanningIndicator(isScanning.value)
            TimeText()
            DeviceList(devicesFound)
        }
    }
}

@Composable
fun DeviceList(devicesFound: MutableState<List<String>>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(devicesFound.value) { deviceInfo ->
            DeviceCard(deviceInfo)
        }
    }
}

@Composable
fun DeviceCard(deviceInfo: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = deviceInfo,
                style = MaterialTheme.typography.bodyMedium // 使用 Material Design 3 的 bodyLarge
            )
        }
    }
}

@Composable
fun ScanningIndicator(isScanning: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(), // 使用 Box 包裹 CircularProgressIndicator
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            CircularProgressIndicator()
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val devicesList = remember { mutableStateOf(listOf("Preview Device 1", "Preview Device 2")) }
    val isScanning = remember { mutableStateOf(false) } // 添加假的 isScanning 狀態
    WearApp(devicesList, isScanning)
}