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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Https
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.example.data.HttpInspectionResult
import com.example.data.NetworkInfoData
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
fun HttpAnalyzerScreen(
    networkInfo: NetworkInfoData,
    result: HttpInspectionResult?,
    isAnalyzing: Boolean,
    isArabic: Boolean,
    onAnalyze: (String) -> Unit,
    onRefreshNetwork: () -> Unit,
    onToggleLang: () -> Unit
) {
    var urlInput by remember { mutableStateOf("192.168.1.1") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("http_analyzer_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefreshNetwork,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Target Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isArabic) "محلل بروتوكول HTTP / HTTPS وفحص TLS" else "HTTP / HTTPS & TLS Inspector",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic) "فحص واجهات الويب، ترويسات الأمان، وشهادات SSL/TLS الرقمية" else "Audit web headers, security policies, and SSL/TLS certificates",
                    fontSize = 12.sp,
                    color = CyberTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("http_url_input"),
                        label = { Text(if (isArabic) "رابط أو عنوان IP" else "URL or Host IP") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberOutline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onAnalyze(urlInput.trim()) },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("analyze_http_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isArabic) "فحص" else "Inspect", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Results Section
        result?.let { res ->
            if (res.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDanger.copy(alpha = 0.1f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberDanger))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberDanger)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = if (isArabic) "فشل الاتصال بالهدف" else "Connection Failed", fontWeight = FontWeight.Bold, color = CyberDanger)
                            Text(text = res.error, fontSize = 12.sp, color = CyberTextSecondary)
                        }
                    }
                }
            } else {
                // Status Overview Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val lockIcon = if (res.isHttps) Icons.Default.Lock else Icons.Default.LockOpen
                                val lockColor = if (res.isHttps) CyberSuccess else CyberDanger

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(lockColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = lockIcon, contentDescription = null, tint = lockColor, modifier = Modifier.size(20.dp))
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "${res.responseCode} ${if (res.responseCode in 200..299) "OK" else ""}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (res.responseCode in 200..299) CyberSuccess else CyberWarning
                                    )
                                    Text(text = res.protocol, fontSize = 12.sp, color = CyberTextSecondary)
                                }
                            }

                            StatusBadge(
                                text = "${res.latencyMs} ms",
                                color = if (res.latencyMs < 300) CyberSuccess else CyberWarning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Security Headers Audit Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isArabic) "تدقيق ترويسات الأمان (Security Headers Audit)" else "Security Headers Audit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Found Headers
                        if (res.securityHeadersFound.isNotEmpty()) {
                            Text(text = if (isArabic) "ترويسات الحماية الموجودة:" else "Active Security Headers:", fontSize = 11.sp, color = CyberSuccess, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            res.securityHeadersFound.forEach { h ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = h, fontSize = 12.sp, color = CyberTextPrimary, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Missing Headers
                        if (res.securityHeadersMissing.isNotEmpty()) {
                            Text(text = if (isArabic) "ترويسات حماية مفقودة (توصية بالإضافة):" else "Missing Recommended Headers:", fontSize = 11.sp, color = CyberWarning, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            res.securityHeadersMissing.forEach { h ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberWarning, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = h, fontSize = 12.sp, color = CyberTextSecondary, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                // TLS Certificate Telemetry (if available)
                res.tlsCertInfo?.let { cert ->
                    Spacer(modifier = Modifier.height(14.dp))
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
                                    text = if (isArabic) "بيانات شهادة TLS الرقمية" else "TLS Certificate Telemetry",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                if (cert.isSelfSigned) {
                                    StatusBadge(text = if (isArabic) "موقعة ذاتياً" else "Self-Signed", color = CyberWarning)
                                } else if (cert.isExpired) {
                                    StatusBadge(text = if (isArabic) "منتهية الصلاحية" else "Expired", color = CyberDanger)
                                } else {
                                    StatusBadge(text = if (isArabic) "صالحة" else "Valid", color = CyberSuccess)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Subject: ${cert.subject}", fontSize = 11.sp, color = CyberTextPrimary, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Issuer: ${cert.issuer}", fontSize = 11.sp, color = CyberTextSecondary, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Validity: ${cert.validFrom} to ${cert.validTo}", fontSize = 11.sp, color = CyberTextSecondary, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Algorithm: ${cert.signatureAlgorithm} (SN: ${cert.serialNumber})", fontSize = 10.sp, color = CyberTextSecondary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Raw Headers Dump
                Spacer(modifier = Modifier.height(14.dp))
                Text(text = if (isArabic) "ترويسات الرد الكاملة (Raw Response Headers):" else "Full Response Headers:", fontSize = 12.sp, color = CyberTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                val rawHeaders = res.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                TerminalBox(content = rawHeaders)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
