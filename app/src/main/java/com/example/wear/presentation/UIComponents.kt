package com.example.wear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.presentation.theme.WearTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.Button

@Composable
fun WearApp(devicesFound: MutableState<List<String>>, isScanning: MutableState<Boolean>, onStartScan: () -> Unit, onStopScan: () -> Unit, onClearAndRescan: () -> Unit) {
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    WearTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            ScanningIndicator(isScanning.value)
            TimeText()
            DeviceList(devicesFound, listState, focusRequester)
            ControlButtons(isScanning, onStartScan, onStopScan, onClearAndRescan)
        }
    }
}

@Composable
fun ControlButtons(isScanning: MutableState<Boolean>, onStartScan: () -> Unit, onStopScan: () -> Unit, onClearAndRescan: () -> Unit) {
    Column {
        Button(onClick = {
            if (isScanning.value) {
                onStopScan()
            } else {
                onStartScan()
            }
        }) {
            Text(if (isScanning.value) "停止掃描" else "開始掃描")
        }
        Button(onClick = onClearAndRescan) {
            Text("清空並重新掃描")
        }
    }
}

// ...保留其他@Composable函數不變...

@Composable
fun DeviceList(devicesFound: MutableState<List<String>>, listState: LazyListState, focusRequester: FocusRequester) {
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent { event ->
                coroutineScope.launch {
                    listState.scrollBy(event.verticalScrollPixels)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(devicesFound.value) { deviceInfo ->
            DeviceCard(deviceInfo) {
                // TODO: 添加點擊後的操作
            }
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun DeviceCard(deviceInfo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = deviceInfo,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ScanningIndicator(isScanning: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
    val isScanning = remember { mutableStateOf(false) }

    // 為預覽提供空的行為實現
    val onStartScan = {}
    val onStopScan = {}
    val onClearAndRescan = {}

    WearApp(devicesList, isScanning, onStartScan, onStopScan, onClearAndRescan)
}
