package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.NetworkUtils
import com.example.data.NetworkInfoData
import com.example.ui.components.CyberTopDashboard
import com.example.ui.components.TerminalBox
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun AdvancedScreen(
    networkInfo: NetworkInfoData,
    isArabic: Boolean,
    onRefreshNetwork: () -> Unit,
    onToggleLang: () -> Unit
) {
    var arpEntries by remember { mutableStateOf(NetworkUtils.getArpTable()) }
    var interfaceList by remember { mutableStateOf(NetworkUtils.getAllNetworkInterfaces()) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("advanced_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefreshNetwork,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ARP Table Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "جدول ARP ومطابقة العناوين الفزيائية" else "ARP Cache Table (/proc/net/arp)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CyberTextPrimary
                    )
                    Button(
                        onClick = { arpEntries = NetworkUtils.getArpTable() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (arpEntries.isEmpty()) {
                    Text(
                        text = if (isArabic) "جدول ARP غير متاح أو تم حجبه بواسطة نظام التشغيل (Android 10+)" else "ARP table empty or restricted by OS permissions",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )
                } else {
                    arpEntries.forEach { (ip, mac) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = ip, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = CyberTextPrimary)
                            Text(text = mac, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = CyberSuccess)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Network Interfaces Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isArabic) "واجهات الشبكة بالنظام (Network Interfaces)" else "System Network Interfaces",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CyberTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                interfaceList.forEach { intf ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${intf["name"]} (${intf["displayName"]})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyberPrimary
                        )
                        Text(
                            text = "Addresses: ${intf["addresses"]}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = "Up: ${intf["isUp"]} • Loopback: ${intf["isLoopback"]}",
                            fontSize = 10.sp,
                            color = CyberTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
