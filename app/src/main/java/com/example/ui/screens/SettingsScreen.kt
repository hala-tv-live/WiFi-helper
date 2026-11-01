package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.AppPreferences
import com.example.data.NetworkInfoData
import com.example.ui.components.CyberTopDashboard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    networkInfo: NetworkInfoData,
    preferences: AppPreferences,
    isArabic: Boolean,
    onToggleLang: () -> Unit,
    onSetLocalLabMode: (Boolean) -> Unit,
    onSetTimeout: (Int) -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
    onSetWatchdogEnabled: (Boolean) -> Unit,
    onSetWatchdogInterval: (Int) -> Unit,
    onTrustCurrentDevices: () -> Unit,
    onRefreshNetwork: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefreshNetwork,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Watchdog Background Monitoring Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (preferences.watchdogEnabled) CyberPrimary else CyberOutline
                )
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (preferences.watchdogEnabled) CyberPrimary else CyberTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isArabic) "حارس الشبكة في الخلفية (Network Watchdog)" else "Background Network Watchdog",
                                    fontSize = 13.sp,
                                    color = CyberTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (preferences.watchdogEnabled) {
                                    StatusBadge(text = if (isArabic) "نشط" else "Active", color = CyberSuccess)
                                }
                            }
                            Text(
                                text = if (isArabic) "فحص دوري وإرسال إشعار فوري عند انضمام جهاز غير معروف" else "Periodic sweeps alerting you when unknown devices join LAN",
                                fontSize = 11.sp,
                                color = CyberTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = preferences.watchdogEnabled,
                        onCheckedChange = { onSetWatchdogEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberPrimary,
                            checkedTrackColor = CyberPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("watchdog_toggle_switch")
                    )
                }

                if (preferences.watchdogEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isArabic) "فترة التكرار (Periodic Interval):" else "Check Interval:",
                        fontSize = 11.sp,
                        color = CyberTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 60).forEach { mins ->
                            FilterChip(
                                selected = preferences.watchdogIntervalMinutes == mins,
                                onClick = { onSetWatchdogInterval(mins) },
                                label = { Text("${mins} ${if (isArabic) "دقيقة" else "min"}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${preferences.knownDeviceMacs.size} ${if (isArabic) "أجهزة موثوقة مسجلة" else "trusted devices stored"}",
                            fontSize = 12.sp,
                            color = CyberTextSecondary
                        )
                        OutlinedButton(
                            onClick = onTrustCurrentDevices,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("trust_current_devices_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberPrimary))
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "توثيق الأجهزة المكتشفة" else "Trust Discovered", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Watchdog Alerts Log (if any)
        if (preferences.watchdogAlerts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberWarning))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = CyberWarning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "سجل تنبيهات الأجهزة الجديدة (${preferences.watchdogAlerts.size})" else "Recent Watchdog Alerts (${preferences.watchdogAlerts.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    preferences.watchdogAlerts.take(5).forEach { alert ->
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(alert.timestamp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberSurfaceVariant)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = alert.ip, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = CyberDanger)
                                Text(text = timeStr, fontSize = 10.sp, color = CyberTextSecondary)
                            }
                            Text(text = "Vendor: ${alert.vendor} • MAC: ${alert.mac}", fontSize = 11.sp, color = CyberTextSecondary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Standard Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isArabic) "خيارات وإعدادات التطبيق" else "Application Preferences",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CyberTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = if (isArabic) "لغة الواجهة" else "Display Language", fontSize = 13.sp, color = CyberTextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = if (isArabic) "العربية (افتراضي)" else "English", fontSize = 11.sp, color = CyberTextSecondary)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = isArabic,
                            onClick = { if (!isArabic) onToggleLang() },
                            label = { Text("عربي", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = !isArabic,
                            onClick = { if (isArabic) onToggleLang() },
                            label = { Text("EN", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Local Lab Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = if (isArabic) "وضع المختبر المحلي (Local Lab Mode)" else "Local Lab Mode", fontSize = 13.sp, color = CyberTextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isArabic) "حصر الفحص على 127.0.0.1 و 10.0.2.2 (المحاكي)" else "Restricts scan to 127.0.0.1 & emulator host",
                                fontSize = 11.sp,
                                color = CyberTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = preferences.localLabMode,
                        onCheckedChange = { onSetLocalLabMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimary, checkedTrackColor = CyberPrimary.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dark Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.Brightness4, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = if (isArabic) "الوضع الداكن (Dark Theme)" else "Dark Theme", fontSize = 13.sp, color = CyberTextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = if (isArabic) "مفعل افتراضياً لتصميم Cyber Security" else "Default high-contrast cyber theme", fontSize = 11.sp, color = CyberTextSecondary)
                        }
                    }

                    Switch(
                        checked = preferences.isDarkMode,
                        onCheckedChange = { onSetDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberPrimary, checkedTrackColor = CyberPrimary.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Socket Timeout
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = if (isArabic) "مهلة اتصال المقبس (Socket Timeout)" else "Socket Connect Timeout", fontSize = 13.sp, color = CyberTextPrimary, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1000, 2000, 3000).forEach { timeout ->
                            FilterChip(
                                selected = preferences.timeoutMs == timeout,
                                onClick = { onSetTimeout(timeout) },
                                label = { Text("${timeout}ms", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // About & Legal Notice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Network Security Lab v2.0 (Audit & Watchdog)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyberTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic) {
                        "أداة تدقيق أمني دفاعية ومحلية 100%. تشمل كشف ثغرات CVE ومولد تقارير PDF متقدم مع حارس شبكة نشط في الخلفية."
                    } else {
                        "100% offline, privacy-first defensive security auditor featuring CVE knowledge base correlation, native PDF reports, and background network watchdog."
                    },
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = CyberTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
