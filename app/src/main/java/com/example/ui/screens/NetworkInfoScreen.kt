package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NetworkInfoData
import com.example.ui.AppScreen
import com.example.ui.components.CyberTopDashboard
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun NetworkInfoScreen(
    networkInfo: NetworkInfoData,
    isArabic: Boolean,
    onRefresh: () -> Unit,
    onToggleLang: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onScanSelf: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onRefresh()
            Toast.makeText(
                context,
                if (isArabic) "تم منح إذن الموقع — جاري قراءة SSID" else "Permission granted — refreshing SSID",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("network_info_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefresh,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate(AppScreen.DEVICE_DISCOVERY) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("nav_device_discovery_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "اكتشاف الأجهزة" else "Discover Hosts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { onScanSelf(networkInfo.localIp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("scan_self_ports_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberPrimary)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "فحص منافذ الجهاز" else "Audit Device",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Network Parameters Section
        Text(
            text = if (isArabic) "معايير الشبكة المحلية" else "Local Network Parameters",
            style = MaterialTheme.typography.titleSmall.copy(
                color = CyberTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                NetworkDetailRow(
                    icon = Icons.Default.Router,
                    label = if (isArabic) "عنوان IP المحلي" else "Local IP Address",
                    value = networkInfo.localIp,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(networkInfo.localIp))
                        Toast.makeText(context, if (isArabic) "تم نسخ IP" else "IP Copied", Toast.LENGTH_SHORT).show()
                    }
                )
                DetailDivider()
                NetworkDetailRow(
                    icon = Icons.Default.AltRoute,
                    label = if (isArabic) "البوابة الافتراضية (Gateway)" else "Default Gateway",
                    value = networkInfo.gatewayIp,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(networkInfo.gatewayIp))
                        Toast.makeText(context, if (isArabic) "تم نسخ Gateway" else "Gateway Copied", Toast.LENGTH_SHORT).show()
                    }
                )
                DetailDivider()
                NetworkDetailRow(
                    icon = Icons.Default.Layers,
                    label = if (isArabic) "قناع الشبكة الفرعية" else "Subnet Mask / CIDR",
                    value = "${networkInfo.subnetMask} (${networkInfo.networkCidr})"
                )
                DetailDivider()
                NetworkDetailRow(
                    icon = Icons.Default.Dns,
                    label = if (isArabic) "خوادم DNS النشطة" else "Active DNS Servers",
                    value = if (networkInfo.dnsServers.isNotEmpty()) networkInfo.dnsServers.joinToString(", ") else "None"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wi-Fi & Interface Details Section
        Text(
            text = if (isArabic) "بيانات واجهة الاتصال و Wi-Fi" else "Interface & Wi-Fi Telemetry",
            style = MaterialTheme.typography.titleSmall.copy(
                color = CyberTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                NetworkDetailRow(
                    icon = Icons.Default.Wifi,
                    label = if (isArabic) "اسم الشبكة (SSID)" else "Network Name (SSID)",
                    value = networkInfo.ssid
                )
                DetailDivider()
                NetworkDetailRow(
                    icon = Icons.Default.Info,
                    label = if (isArabic) "عنوان BSSID (نقطة الوصول)" else "BSSID (Access Point)",
                    value = networkInfo.bssid
                )
                DetailDivider()
                NetworkDetailRow(
                    icon = Icons.Default.Speed,
                    label = if (isArabic) "سرعة الرابط والتردد" else "Link Speed & Frequency",
                    value = if (networkInfo.linkSpeedMbps > 0)
                        "${networkInfo.linkSpeedMbps} Mbps (${networkInfo.frequencyMhz} MHz)"
                    else
                        "N/A"
                )
                DetailDivider()
                NetworkDetailRow(
                    icon = Icons.Default.Layers,
                    label = if (isArabic) "اسم الواجهة بالهاتف" else "Interface Name",
                    value = networkInfo.interfaceName
                )
            }
        }

        // SSID Permission Banner if unavailable
        if (networkInfo.ssid == "Not Connected" || networkInfo.ssid == "<unknown ssid>" || networkInfo.ssid == "Wi-Fi Active") {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CyberPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "إذن الموقع مطلوب لقراءة SSID" else "Location Permission Needed for SSID",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = if (isArabic) "انقر هنا لمنح الإذن وفق متطلبات نظام أندرويد" else "Tap to grant permission as required by Android OS",
                            fontSize = 11.sp,
                            color = CyberTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NetworkDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onCopy != null) { onCopy?.invoke() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = CyberTextSecondary
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = CyberTextPrimary
                )
            }
        }
    }
}

@Composable
private fun DetailDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CyberOutline.copy(alpha = 0.4f))
            .padding(vertical = 4.dp)
    )
}
