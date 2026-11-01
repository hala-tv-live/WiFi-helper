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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DnsInspectionResult
import com.example.data.NetworkInfoData
import com.example.data.PingResult
import com.example.data.TracerouteHop
import com.example.ui.components.CyberTopDashboard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TerminalBox
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberInfo
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning

@Composable
fun ToolsScreen(
    networkInfo: NetworkInfoData,
    dnsResult: DnsInspectionResult?,
    isTestingDns: Boolean,
    pingResult: PingResult?,
    isPinging: Boolean,
    tracerouteHops: List<TracerouteHop>,
    isTracing: Boolean,
    isArabic: Boolean,
    onTestDns: (String) -> Unit,
    onRunPing: (String) -> Unit,
    onRunTraceroute: (String) -> Unit,
    onRefreshNetwork: () -> Unit,
    onToggleLang: () -> Unit
) {
    var selectedToolTab by remember { mutableStateOf(0) } // 0: Ping, 1: DNS, 2: Traceroute
    var pingHostInput by remember { mutableStateOf(if (networkInfo.gatewayIp != "Unknown") networkInfo.gatewayIp else "8.8.8.8") }
    var dnsDomainInput by remember { mutableStateOf("google.com") }
    var traceHostInput by remember { mutableStateOf("1.1.1.1") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("tools_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefreshNetwork,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tool Tabs
        TabRow(
            selectedTabIndex = selectedToolTab,
            containerColor = CyberSurface,
            contentColor = CyberPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedToolTab]),
                    color = CyberPrimary
                )
            }
        ) {
            Tab(
                selected = selectedToolTab == 0,
                onClick = { selectedToolTab = 0 },
                text = { Text(if (isArabic) "فحص Ping" else "Ping Tool", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.NetworkPing, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedToolTab == 1,
                onClick = { selectedToolTab = 1 },
                text = { Text(if (isArabic) "اختبار DNS" else "DNS Tester", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedToolTab == 2,
                onClick = { selectedToolTab = 2 },
                text = { Text(if (isArabic) "تتبع المسار" else "Traceroute", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content
        when (selectedToolTab) {
            0 -> {
                // Ping Tool
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isArabic) "فحص زمن الاستجابة (ICMP Ping Sweep)" else "Latency & Packet Loss Diagnostic",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyberTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pingHostInput,
                                onValueChange = { pingHostInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ping_host_input"),
                                label = { Text(if (isArabic) "الهدف (Gateway أو IP)" else "Host / IP") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberPrimary,
                                    unfocusedBorderColor = CyberOutline
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onRunPing(pingHostInput.trim()) },
                                enabled = !isPinging,
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("run_ping_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isPinging) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ping", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                pingResult?.let { res ->
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
                                    text = "${if (isArabic) "إحصائيات الاتصال بـ" else "Ping Statistics for"} ${res.host}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                StatusBadge(
                                    text = "${res.packetLossPercent}% Loss",
                                    color = if (res.packetLossPercent == 0) CyberSuccess else CyberDanger
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PingStatItem("Min", "${res.minRttMs} ms")
                                PingStatItem("Avg", "${res.avgRttMs} ms")
                                PingStatItem("Max", "${res.maxRttMs} ms")
                                PingStatItem("Packets", "${res.packetsReceived}/${res.packetsSent}")
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = if (isArabic) "مخرجات الفحص:" else "Raw Terminal Output:", fontSize = 11.sp, color = CyberTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            TerminalBox(content = res.outputLog)
                        }
                    }
                }
            }

            1 -> {
                // DNS Tester
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isArabic) "اختبار تحليل أسماء النطاقات (DNS Resolver)" else "DNS Resolution & Latency Diagnostic",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyberTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = dnsDomainInput,
                                onValueChange = { dnsDomainInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dns_domain_input"),
                                label = { Text(if (isArabic) "اسم النطاق (مثال: google.com)" else "Domain (e.g. google.com)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberPrimary,
                                    unfocusedBorderColor = CyberOutline
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onTestDns(dnsDomainInput.trim()) },
                                enabled = !isTestingDns,
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("run_dns_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isTestingDns) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isArabic) "استعلام" else "Resolve", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                dnsResult?.let { res ->
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
                                    text = res.domain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CyberTextPrimary
                                )
                                StatusBadge(text = "${res.latencyMs} ms", color = CyberSuccess)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (res.error != null) {
                                Text(text = res.error, color = CyberDanger, fontSize = 12.sp)
                            } else {
                                Text(
                                    text = "${if (isArabic) "الاسم القياسي (Canonical)" else "Canonical Host"}: ${res.canonicalName}",
                                    fontSize = 12.sp,
                                    color = CyberTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isArabic) "العناوين المستخرجة (IP Records):" else "Resolved IP Addresses:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.addresses.forEach { addr ->
                                    Text(
                                        text = "• $addr",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = CyberTextPrimary,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Traceroute
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isArabic) "تتبع مسار الحزم (Traceroute Diagnostic)" else "Hop-by-Hop Traceroute",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyberTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "تنبيه: نظراً لقيود حماية أندرويد لغير الـ Root، قد لا تستجيب بعض الموجهات المتوسطة." else "Notice: Due to unprivileged Android constraints, intermediate hops may hide ICMP responses.",
                            fontSize = 11.sp,
                            color = CyberWarning
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = traceHostInput,
                                onValueChange = { traceHostInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("trace_host_input"),
                                label = { Text(if (isArabic) "الهدف المراد تتبعه" else "Target Host / IP") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberPrimary,
                                    unfocusedBorderColor = CyberOutline
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onRunTraceroute(traceHostInput.trim()) },
                                enabled = !isTracing,
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("run_trace_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isTracing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (isArabic) "تتبع" else "Trace", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (tracerouteHops.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isArabic) "قفزات المسار المكتشفة:" else "Route Hops:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            tracerouteHops.forEach { hop ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${hop.hopNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = CyberPrimary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = hop.ip,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            color = if (hop.isReached) CyberSuccess else CyberTextPrimary
                                        )
                                    }
                                    Text(
                                        text = "${hop.rttMs} ms",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = CyberTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PingStatItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 10.sp, color = CyberTextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CyberTextPrimary)
    }
}
