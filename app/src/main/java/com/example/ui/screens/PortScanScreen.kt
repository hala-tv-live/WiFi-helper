package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.PortScanner
import com.example.data.NetworkInfoData
import com.example.data.PortScanItem
import com.example.data.PortState
import com.example.data.TlsCertInfo
import com.example.ui.components.CyberProgressBar
import com.example.ui.components.CyberTopDashboard
import com.example.ui.components.PortStatusBadge
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
fun PortScanScreen(
    networkInfo: NetworkInfoData,
    targetIp: String,
    results: List<PortScanItem>,
    isScanning: Boolean,
    progress: Pair<Int, Int>,
    inspectedBanner: String?,
    inspectedTls: TlsCertInfo?,
    isInspectingService: Boolean,
    isArabic: Boolean,
    onStartScan: (targetIp: String, ports: List<Int>) -> Unit,
    onStopScan: () -> Unit,
    onInspectPort: (ip: String, port: Int) -> Unit,
    onRefreshNetwork: () -> Unit,
    onToggleLang: () -> Unit
) {
    var ipInput by remember(targetIp) { mutableStateOf(targetIp) }
    var selectedPreset by remember { mutableStateOf(0) } // 0: Common (14), 1: Security (32), 2: Top 100, 3: Custom
    var customRangeInput by remember { mutableStateOf("1-1024") }
    var statusFilter by remember { mutableStateOf<PortState?>(null) } // null = All
    var selectedPortForInspection by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("port_scan_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefreshNetwork,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Scanner Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isArabic) "فاحص المنافذ التفاعلي (TCP Connect)" else "Interactive TCP Port Scanner",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CyberTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target IP Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("target_ip_input"),
                        label = { Text(if (isArabic) "عنوان IP الهدف" else "Target Host / IP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberOutline,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (!isScanning) {
                        Button(
                            onClick = {
                                val ports = when (selectedPreset) {
                                    0 -> PortScanner.COMMON_PORTS
                                    1 -> PortScanner.EXTENDED_SECURITY_PORTS
                                    2 -> (1..100).toList()
                                    else -> parseCustomPorts(customRangeInput)
                                }
                                onStartScan(ipInput.trim(), ports)
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("start_port_scan_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "فحص" else "Scan", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onStopScan,
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("stop_port_scan_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDanger),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "إلغاء" else "Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Port Presets Selection
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(
                        Pair(if (isArabic) "الشائعة (14)" else "Common (14)", 0),
                        Pair(if (isArabic) "الأمان الموسع (32)" else "Security (32)", 1),
                        Pair(if (isArabic) "أول 100 منفذ" else "Top 100", 2),
                        Pair(if (isArabic) "نطاق مخصص" else "Custom Range", 3)
                    )
                    items(presets) { (title, index) ->
                        FilterChip(
                            selected = selectedPreset == index,
                            onClick = { selectedPreset = index },
                            label = { Text(title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = CyberSurfaceVariant,
                                labelColor = CyberTextSecondary
                            )
                        )
                    }
                }

                // If Custom Range is selected
                if (selectedPreset == 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customRangeInput,
                        onValueChange = { customRangeInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_port_range_input"),
                        label = { Text(if (isArabic) "أدخل نطاقاً (مثال: 1-1024 أو 80,443,8080)" else "Enter range (e.g. 1-1024 or 80,443,8080)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberOutline
                        )
                    )
                }

                if (isScanning || progress.first > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CyberProgressBar(
                        scanned = progress.first,
                        total = progress.second,
                        label = if (isArabic) "جاري فحص المنافذ بالتوازي (Thread pool 50)..." else "Connecting across thread pool 50..."
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val openCount = results.count { it.state == PortState.OPEN }
            val closedCount = results.count { it.state == PortState.CLOSED }
            val filteredCount = results.count { it.state == PortState.FILTERED }

            Text(
                text = "${results.size} ${if (isArabic) "نتيجة" else "results"} ($openCount ${if (isArabic) "مفتوح" else "open"})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text(if (isArabic) "الكل" else "All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberOutline,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = statusFilter == PortState.OPEN,
                    onClick = { statusFilter = if (statusFilter == PortState.OPEN) null else PortState.OPEN },
                    label = { Text(if (isArabic) "مفتوح ($openCount)" else "Open ($openCount)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberSuccess,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Service Inspection Panel (if a port was tapped)
        if (selectedPortForInspection != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .testTag("service_inspection_panel"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131720)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberPrimary))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isArabic) "فحص الخدمة لمنفذ" else "Service Inspector for Port"} $selectedPortForInspection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberPrimary
                            )
                        }
                        IconButton(
                            onClick = { selectedPortForInspection = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = CyberTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isInspectingService) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (isArabic) "جاري قراءة رد الخدمة (Banner Grabbing)..." else "Capturing service banner...", fontSize = 12.sp, color = CyberTextSecondary)
                        }
                    } else {
                        inspectedBanner?.let { banner ->
                            Text(text = if (isArabic) "رد الخدمة الأولي (Banner):" else "Raw Service Banner:", fontSize = 11.sp, color = CyberTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            TerminalBox(content = banner)
                        }

                        inspectedTls?.let { tls ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = if (isArabic) "شهادة TLS الرقمية:" else "TLS Certificate Telemetry:", fontSize = 11.sp, color = CyberTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberSurfaceVariant)
                                    .padding(8.dp)
                            ) {
                                Text("Subject: ${tls.subject}", fontSize = 11.sp, color = CyberTextPrimary, fontFamily = FontFamily.Monospace)
                                Text("Issuer: ${tls.issuer}", fontSize = 11.sp, color = CyberTextSecondary, fontFamily = FontFamily.Monospace)
                                Text("Valid: ${tls.validFrom} -> ${tls.validTo}", fontSize = 11.sp, color = CyberTextSecondary, fontFamily = FontFamily.Monospace)
                                if (tls.isExpired) {
                                    Text("⚠️ EXPIRED CERTIFICATE", fontSize = 11.sp, color = CyberDanger, fontWeight = FontWeight.Bold)
                                }
                                if (tls.isSelfSigned) {
                                    Text("⚠️ SELF-SIGNED CERTIFICATE", fontSize = 11.sp, color = CyberWarning, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Port Results List
        val filteredResults = results.filter { statusFilter == null || it.state == statusFilter }

        if (filteredResults.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "لا توجد نتائج مطابقة — حدد الهدف واضغط 'فحص'" else "No port results — configure target and tap 'Scan'",
                    color = CyberTextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("port_results_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredResults, key = { it.port }) { item ->
                    PortRowItem(
                        item = item,
                        isArabic = isArabic,
                        onClick = {
                            if (item.state == PortState.OPEN) {
                                selectedPortForInspection = item.port
                                onInspectPort(ipInput.trim(), item.port)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PortRowItem(
    item: PortScanItem,
    isArabic: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("port_row_${item.port}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (item.state == PortState.OPEN) CyberSuccess.copy(alpha = 0.5f) else CyberOutline
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.port}",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = if (item.state == PortState.OPEN) CyberSuccess else CyberTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.serviceName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = CyberTextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.responseTimeMs} ms",
                            fontSize = 11.sp,
                            color = CyberTextSecondary
                        )
                        if (item.matchedCves.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(
                                text = "${item.matchedCves.size} CVEs",
                                color = CyberDanger
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PortStatusBadge(state = item.state, isArabic = isArabic)
                if (item.state == PortState.OPEN) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Inspect",
                        tint = CyberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun parseCustomPorts(input: String): List<Int> {
    val clean = input.trim()
    val ports = mutableListOf<Int>()
    if (clean.contains("-")) {
        val start = clean.substringBefore("-").trim().toIntOrNull() ?: 1
        val end = clean.substringAfter("-").trim().toIntOrNull() ?: 1024
        for (p in start.coerceAtLeast(1)..end.coerceAtMost(65535)) {
            ports.add(p)
            if (ports.size >= 1000) break // Safe limit
        }
    } else {
        val tokens = clean.split(",")
        for (t in tokens) {
            t.trim().toIntOrNull()?.let {
                if (it in 1..65535) ports.add(it)
            }
        }
    }
    return if (ports.isNotEmpty()) ports else PortScanner.COMMON_PORTS
}
