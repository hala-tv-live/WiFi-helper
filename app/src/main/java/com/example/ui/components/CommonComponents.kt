package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConnectionType
import com.example.data.FindingSeverity
import com.example.data.NetworkInfoData
import com.example.data.PortState
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberInfo
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWarning

@Composable
fun CyberTopDashboard(
    networkInfo: NetworkInfoData,
    isArabic: Boolean,
    onRefresh: () -> Unit,
    onToggleLang: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cyber_top_dashboard"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Connection Icon & IP
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (connIcon, connColor) = when (networkInfo.connectionType) {
                    ConnectionType.WIFI -> Pair(Icons.Default.SignalWifi4Bar, CyberSuccess)
                    ConnectionType.CELLULAR -> Pair(Icons.Default.CellTower, CyberInfo)
                    ConnectionType.ETHERNET -> Pair(Icons.Default.SignalWifi4Bar, CyberSuccess)
                    ConnectionType.OFFLINE -> Pair(Icons.Default.SignalWifiOff, CyberDanger)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(connColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = connIcon,
                        contentDescription = "Connection Status",
                        tint = connColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = networkInfo.localIp,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                color = CyberTextPrimary
                            )
                        )
                    }
                    Text(
                        text = if (networkInfo.connectionType == ConnectionType.WIFI) networkInfo.ssid else networkInfo.interfaceName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Controls: Language Toggle & Refresh
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberBackground)
                        .border(1.dp, CyberOutline, RoundedCornerShape(8.dp))
                        .clickable { onToggleLang() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("toggle_lang_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = CyberPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "EN" else "عربي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("refresh_network_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = CyberPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PortStatusBadge(state: PortState, isArabic: Boolean) {
    val (label, color) = when (state) {
        PortState.OPEN -> Pair(if (isArabic) "مفتوح" else "OPEN", CyberSuccess)
        PortState.CLOSED -> Pair(if (isArabic) "مغلق" else "CLOSED", CyberDanger)
        PortState.FILTERED -> Pair(if (isArabic) "مُرشّح" else "FILTERED", CyberWarning)
    }
    StatusBadge(text = label, color = color)
}

@Composable
fun SeverityBadge(severity: FindingSeverity, isArabic: Boolean) {
    val (label, color) = when (severity) {
        FindingSeverity.CRITICAL -> Pair(if (isArabic) "حرج" else "CRITICAL", CyberDanger)
        FindingSeverity.HIGH -> Pair(if (isArabic) "مرتفع" else "HIGH", CyberDanger)
        FindingSeverity.MEDIUM -> Pair(if (isArabic) "متوسط" else "MEDIUM", CyberWarning)
        FindingSeverity.LOW -> Pair(if (isArabic) "منخفض" else "LOW", CyberInfo)
        FindingSeverity.INFO -> Pair(if (isArabic) "معلومة" else "INFO", CyberTextSecondary)
    }
    StatusBadge(text = label, color = color)
}

@Composable
fun CyberProgressBar(
    scanned: Int,
    total: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) (scanned.toFloat() / total).coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = CyberTextSecondary
            )
            Text(
                text = "$scanned / $total ($percent%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CyberPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CyberPrimary,
            trackColor = CyberOutline
        )
    }
}

@Composable
fun TerminalBox(
    content: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090B0E))
            .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = content,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Color(0xFF4ADE80) // Matrix-green cyber terminal font
        )
    }
}
