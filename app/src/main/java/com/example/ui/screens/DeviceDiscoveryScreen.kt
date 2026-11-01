package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DiscoveredDevice
import com.example.data.NetworkInfoData
import com.example.ui.components.CyberProgressBar
import com.example.ui.components.CyberTopDashboard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberInfo
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun DeviceDiscoveryScreen(
    networkInfo: NetworkInfoData,
    devices: List<DiscoveredDevice>,
    isDiscovering: Boolean,
    progress: Pair<Int, Int>,
    isArabic: Boolean,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onRefreshNetwork: () -> Unit,
    onToggleLang: () -> Unit,
    onSelectDeviceForScan: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("device_discovery_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefreshNetwork,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Scan Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "اكتشاف أجهزة الشبكة (Subnet Sweep)" else "Device Discovery (Subnet Sweep)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "${networkInfo.networkCidr} • ${devices.size} ${if (isArabic) "أجهزة مكتشفة" else "hosts discovered"}",
                            fontSize = 12.sp,
                            color = CyberTextSecondary
                        )
                    }

                    if (!isDiscovering) {
                        Button(
                            onClick = onStartDiscovery,
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("start_discovery_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "بدء الفحص" else "Start Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onStopDiscovery,
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("stop_discovery_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDanger),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "إيقاف" else "Stop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isDiscovering || progress.first > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CyberProgressBar(
                        scanned = progress.first,
                        total = progress.second,
                        label = if (isArabic) "جاري مسح العناوين عبر 32 مسار متزامن..." else "Probing hosts via 32 concurrent threads..."
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device List
        if (devices.isEmpty() && !isDiscovering) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = CyberOutline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "لم يتم فحص الأجهزة بعد" else "No devices discovered yet",
                        color = CyberTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) "اضغط على 'بدء الفحص' لاكتشاف كل الأجهزة المتصلة بالشبكة" else "Tap 'Start Scan' to sweep the local subnet",
                        color = CyberTextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("device_list_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(devices, key = { it.ip }) { device ->
                    DeviceCard(
                        device = device,
                        isArabic = isArabic,
                        onScanPorts = { onSelectDeviceForScan(device.ip) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: DiscoveredDevice,
    isArabic: Boolean,
    onScanPorts: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.ip}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val deviceIcon = when {
                        device.isGateway -> Icons.Default.Router
                        device.isSelf -> Icons.Default.PhoneAndroid
                        else -> Icons.Default.Computer
                    }
                    val iconColor = when {
                        device.isGateway -> CyberInfo
                        device.isSelf -> CyberSuccess
                        else -> CyberPrimary
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = deviceIcon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = device.ip,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = CyberTextPrimary
                            )
                            if (device.isGateway) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(text = if (isArabic) "البوابة" else "Gateway", color = CyberInfo)
                            }
                            if (device.isSelf) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(text = if (isArabic) "هذا الجهاز" else "This Device", color = CyberSuccess)
                            }
                        }
                        Text(
                            text = device.hostname,
                            fontSize = 12.sp,
                            color = CyberTextSecondary
                        )
                    }
                }

                // Scan Ports Action
                OutlinedButton(
                    onClick = onScanPorts,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("scan_ports_btn_${device.ip}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberPrimary)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "فحص المنافذ" else "Audit Ports", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-details: MAC, Vendor, Ping
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${device.vendor} • MAC: ${device.macAddress}",
                    fontSize = 11.sp,
                    color = CyberTextSecondary
                )
                Text(
                    text = "${device.responseTimeMs} ms",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberSuccess
                )
            }
        }
    }
}
