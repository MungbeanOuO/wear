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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.presentation.theme.WearTheme

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var devicesFound: MutableState<List<String>>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        devicesFound = mutableStateOf(listOf())

        // 初始化ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // 權限被授予，可以進行BLE掃描
                startBleScan()
            } else {
                // 權限被拒絕，處理您的邏輯
            }
        }

        // 請求權限
        requestPermissions()

        setContent {
            WearApp(devicesFound)
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }


    private fun startBleScan() {
        // 檢查精確位置權限是否已被授予
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 如果沒有權限，處理權限請求或SecurityException
            // 這裡可以顯示一個解釋為什麼需要這些權限的對話框，或者禁用BLE掃描功能
            return
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                // 檢查藍牙連接權限是否已被授予
                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // 如果沒有權限，處理權限請求或SecurityException
                    return
                }
                val deviceName = result.device.name ?: "Unknown Device"
                val deviceInfo = "Device Name: $deviceName\n" +
                        "Device Address: ${result.device.address}\n" +
                        "Signal Strength: ${result.rssi}dBm\n"
                // 使用運算符賦值來簡化代碼
                devicesFound.value += deviceInfo
            }
        }

        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

}

@Composable
fun WearApp(devicesFound: MutableState<List<String>>) {
    WearTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            DeviceList(devicesFound)
        }
    }
}

@Composable
fun DeviceList(devicesFound: MutableState<List<String>>) {
    for (deviceInfo in devicesFound.value) {
        androidx.wear.compose.material.Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = deviceInfo
        )
    }
}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val devicesList = remember { mutableStateOf(listOf("Preview Device 1", "Preview Device 2")) }
    WearApp(devicesList)
}