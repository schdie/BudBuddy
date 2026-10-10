package com.benegedeniz.budsdynamiceq.ui.buds

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.benegedeniz.budsdynamiceq.R
import com.benegedeniz.budsdynamiceq.data.model.NoiseControlMode
import com.benegedeniz.budsdynamiceq.data.model.PlacementState
import com.benegedeniz.budsdynamiceq.ui.components.ConnectingDialog
import com.benegedeniz.budsdynamiceq.ui.components.DeviceSelectionDialog
import androidx.compose.foundation.clickable

@Composable
fun BudsScreen(
    viewModel: BudsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val isConnected = uiState.isConnected
    val isConnecting = uiState.isConnecting
    val pairedDevices = uiState.pairedDevices
    val savedDeviceMac = uiState.savedDeviceMac
    val effectiveModel = uiState.effectiveModel
    val activeNoiseControl = uiState.activeNoiseControl ?: NoiseControlMode.OFF

    var showDeviceDialog by remember { mutableStateOf(false) }
    var showConnectingDialog by remember { mutableStateOf(false) }
    var showDeviceMenu by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.setHomePageVisible(true)
        onDispose { viewModel.setHomePageVisible(false) }
    }

    val savedDeviceName = remember(savedDeviceMac, pairedDevices) {
        pairedDevices.find { it.address == savedDeviceMac }?.let {
            try { it.name } catch (e: SecurityException) { null }
        } ?: savedDeviceMac
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // Title
            item {
                Text(
                    text = stringResource(R.string.bud_buddy),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.statusBarsPadding().padding(start = 8.dp, bottom = 16.dp)
                )
            }

            // Connection + battery
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = when {
                                            isConnected -> stringResource(R.string.buds_connected)
                                            isConnecting -> if (savedDeviceName != null)
                                                stringResource(R.string.buds_connecting_to, savedDeviceName)
                                            else stringResource(R.string.buds_connecting)
                                            else -> stringResource(R.string.buds_disconnected)
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when {
                                            isConnected -> MaterialTheme.colorScheme.onSurface
                                            isConnecting -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.error
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = if (isConnecting) Modifier.basicMarquee() else Modifier
                                    )
                                    if (isConnecting) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                                if (!isConnected && !isConnecting && savedDeviceName != null) {
                                    Text(
                                        text = stringResource(R.string.buds_saved, savedDeviceName),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee()
                                    )
                                }
                            }

                            // Connect / Disconnect / Cancel
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isConnected) {
                                    TextButton(
                                        onClick = { viewModel.disconnect(forget = false) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(stringResource(R.string.disconnect), color = MaterialTheme.colorScheme.error)
                                    }
                                    Box {
                                        IconButton(onClick = { showDeviceMenu = true }, modifier = Modifier.size(36.dp)) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = stringResource(R.string.device_options),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        DropdownMenu(expanded = showDeviceMenu, onDismissRequest = { showDeviceMenu = false }) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.select_another_device)) },
                                                leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null) },
                                                onClick = {
                                                    showDeviceMenu = false
                                                    viewModel.refreshPairedDevices()
                                                    showDeviceDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.forget_device), color = MaterialTheme.colorScheme.error) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                                },
                                                onClick = {
                                                    showDeviceMenu = false
                                                    viewModel.disconnect(forget = true)
                                                }
                                            )
                                        }
                                    }
                                } else if (isConnecting) {
                                    TextButton(onClick = {
                                        viewModel.disconnect(forget = false)
                                        showConnectingDialog = false
                                    }) {
                                        Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (savedDeviceMac == null) {
                                                viewModel.refreshPairedDevices()
                                                showDeviceDialog = true
                                            } else {
                                                val savedDevice = pairedDevices.find { it.address == savedDeviceMac }
                                                if (savedDevice != null) viewModel.connectToDevice(savedDevice)
                                                else viewModel.startAutoConnect()
                                                showConnectingDialog = true
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(stringResource(R.string.connect))
                                    }
                                }
                            }
                        }

                        // Battery bar
                        if (isConnected && uiState.batteryL >= 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BudBatteryInfo(stringResource(R.string.buds_left), uiState.batteryL, uiState.placementL, uiState.chargingL, uiState.temperatureL)
                                BudBatteryInfo(stringResource(R.string.buds_case), uiState.batteryCase, PlacementState.UNKNOWN, uiState.chargingCase, null)
                                BudBatteryInfo(stringResource(R.string.buds_right), uiState.batteryR, uiState.placementR, uiState.chargingR, uiState.temperatureR)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Noise control (read-only)
            item {
                SectionHeader(title = stringResource(R.string.noise_controls), modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val modes = buildList {
                            add(NoiseControlMode.OFF)
                            if (effectiveModel.supportsTransparencyNC) add(NoiseControlMode.TRANSPARENT)
                            if (effectiveModel.supportsAdaptiveNC) add(NoiseControlMode.ADAPTIVE)
                            add(NoiseControlMode.NOISE_CANCELLATION)
                        }
                        val bothInEar = isConnected &&
                                uiState.placementL == PlacementState.WEARING &&
                                uiState.placementR == PlacementState.WEARING
                        val anyInEar = isConnected &&
                                (uiState.placementL == PlacementState.WEARING || uiState.placementR == PlacementState.WEARING)
                        val isBudsTransparencyAllowed =
                            effectiveModel == com.benegedeniz.budsdynamiceq.bluetooth.BudsModel.BUDS_3_PRO ||
                                    effectiveModel == com.benegedeniz.budsdynamiceq.bluetooth.BudsModel.BUDS_4_PRO

                        modes.forEach { mode ->
                            val isSelected = isConnected && activeNoiseControl == mode
                            val isModeEnabled = when {
                                !isConnected || !anyInEar -> false
                                bothInEar || uiState.oneEarbudNoiseControlEnabled -> true
                                isBudsTransparencyAllowed -> mode == NoiseControlMode.OFF || mode == NoiseControlMode.TRANSPARENT
                                else -> mode == NoiseControlMode.OFF
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = isModeEnabled) { viewModel.applyImmediateNoiseControl(mode) }
                                    .padding(8.dp)
                                    .alpha(if (isModeEnabled) 1f else 0.5f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            NoiseControlMode.NOISE_CANCELLATION -> Icons.AutoMirrored.Filled.VolumeOff
                                            NoiseControlMode.OFF -> Icons.Default.Close
                                            NoiseControlMode.TRANSPARENT -> Icons.Default.Hearing
                                            NoiseControlMode.ADAPTIVE -> Icons.Default.AutoAwesome
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(mode.displayNameRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Other features
            if (isConnected) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                            FeatureToggleRow(
                                label = "Noise control with one earbud",
                                checked = uiState.oneEarbudNoiseControlEnabled,
                                onCheckedChange = { viewModel.setOneEarbudNoiseControl(it) }
                            )
                            if (effectiveModel.supportsTransparencyNC) {
                                FeatureToggleRow(
                                    label = "Ambient sound during calls",
                                    checked = uiState.useAmbientSoundDuringCalls,
                                    onCheckedChange = { viewModel.setUseAmbientSoundDuringCalls(it) }
                                )
                            }
                            FeatureToggleRow(
                                label = "In-ear detection for calls",
                                checked = uiState.inEarDetectionForCalls,
                                onCheckedChange = { viewModel.setInEarDetectionForCalls(it) }
                            )
                            if (effectiveModel.supportsDoubleTapEdge) {
                                FeatureToggleRow(
                                    label = "Double tap earbud edge",
                                    checked = uiState.doubleTapEdgeEnabled,
                                    onCheckedChange = { viewModel.setDoubleTapEdgeEnabled(it) }
                                )
                            }
                            if (effectiveModel.supportsConversationDetection) {
                                FeatureToggleRow(
                                    label = "Voice detect",
                                    checked = uiState.conversationDetectionEnabled,
                                    onCheckedChange = { viewModel.setConversationDetection(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeviceDialog) {
        DeviceSelectionDialog(
            pairedDevices = pairedDevices,
            savedDeviceMac = savedDeviceMac,
            isConnected = isConnected,
            isConnecting = isConnecting,
            isBluetoothEnabled = viewModel.isBluetoothEnabled(),
            onSelectDevice = { device ->
                viewModel.connectToDevice(device)
                showDeviceDialog = false
                showConnectingDialog = true
            },
            onRefresh = { viewModel.refreshPairedDevices() },
            onForgetDevice = { viewModel.disconnect(forget = true) },
            onDismissRequest = { showDeviceDialog = false }
        )
    }

    if (showConnectingDialog) {
        ConnectingDialog(
            deviceName = savedDeviceName ?: stringResource(R.string.buds_galaxy_buds_default),
            macAddress = savedDeviceMac,
            isConnected = isConnected,
            onCancel = {
                viewModel.disconnect(forget = false)
                showConnectingDialog = false
            },
            onForgetAndSelectOther = {
                viewModel.disconnect(forget = true)
                showConnectingDialog = false
                viewModel.refreshPairedDevices()
                showDeviceDialog = true
            },
            onDismissRequest = { showConnectingDialog = false }
        )
    }
}

@Composable
private fun FeatureToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(end = 12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun BudBatteryInfo(label: String, battery: Int, placement: PlacementState, isCharging: Boolean = false, temperature: Double? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val color = if (battery <= 0) {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        } else if (placement == PlacementState.WEARING) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        val isActuallyCharging = isCharging && battery > 0 &&
                (label == "Case" || placement == PlacementState.CASE || placement == PlacementState.CLOSED_CASE)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActuallyCharging) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = stringResource(R.string.charging),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp).padding(end = 2.dp)
                )
            }
            if (battery > 0) {
                Text(text = "$battery%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            } else {
                Text(text = "--", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (temperature != null && battery > 0) {
            Text(
                text = "${String.format("%.1f", temperature)}°C",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}