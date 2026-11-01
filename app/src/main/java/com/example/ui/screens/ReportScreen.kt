package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuditReport
import com.example.data.FindingSeverity
import com.example.data.NetworkInfoData
import com.example.data.ReportGenerator
import com.example.data.SecurityFinding
import com.example.ui.components.CyberTopDashboard
import com.example.ui.components.SeverityBadge
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
import com.example.ui.theme.CyberWarning
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(
    networkInfo: NetworkInfoData,
    report: AuditReport?,
    isGeneratingPdf: Boolean,
    isArabic: Boolean,
    onGenerateReport: () -> Unit,
    onExportPdf: () -> Unit,
    onRefreshNetwork: () -> Unit,
    onToggleLang: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("report_screen")
    ) {
        CyberTopDashboard(
            networkInfo = networkInfo,
            isArabic = isArabic,
            onRefresh = onRefreshNetwork,
            onToggleLang = onToggleLang
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (report == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = CyberPrimary, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "لم يتم توليد تقرير تدقيق حتى الآن" else "No Security Audit Report Generated",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CyberTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "قم بإجراء فحص للأجهزة أو المنافذ ثم اضغط الزر بالأسفل لتجميع النتائج ومطابقة CVE" else "Run port or host scans to correlate findings and CVE vulnerability records",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onGenerateReport,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("generate_initial_report_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isArabic) "توليد تقرير الأمان الآن" else "Generate Audit Report", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Header Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "تقييم الأمان الإجمالي (Security Score)" else "Overall Security Posture",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "Target: ${report.targetIp} (${report.targetNetwork})",
                                fontSize = 12.sp,
                                color = CyberTextSecondary
                            )
                        }

                        // Score Badge
                        val scoreColor = when {
                            report.score >= 80 -> CyberSuccess
                            report.score >= 60 -> CyberWarning
                            else -> CyberDanger
                        }

                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(scoreColor.copy(alpha = 0.15f))
                                .border(2.dp, scoreColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${report.score}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = scoreColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary counts
                    val highCount = report.findings.count { it.severity == FindingSeverity.HIGH || it.severity == FindingSeverity.CRITICAL }
                    val medCount = report.findings.count { it.severity == FindingSeverity.MEDIUM }
                    val lowCount = report.findings.count { it.severity == FindingSeverity.LOW }
                    val cveCount = report.findings.count { it.cve != null }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SeverityCountBox(if (isArabic) "مرتفع" else "High", highCount, CyberDanger)
                        SeverityCountBox(if (isArabic) "متوسط" else "Med", medCount, CyberWarning)
                        SeverityCountBox(if (isArabic) "ثغرات CVE" else "CVEs", cveCount, CyberPrimary)
                        SeverityCountBox(if (isArabic) "منافذ نشطة" else "Ports", report.scannedPorts.count { it.state == com.example.data.PortState.OPEN }, CyberSuccess)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PDF Export Banner Button
            Button(
                onClick = onExportPdf,
                enabled = !isGeneratingPdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("export_official_pdf_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isGeneratingPdf) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isArabic) "جاري إنشاء ملف PDF..." else "Generating PDF Report...")
                } else {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تصدير تقرير PDF رسمي ومشاركته" else "Export Official PDF Audit Report",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export CSV & Share Raw Text Actions
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
                    OutlinedButton(
                        onClick = {
                            val text = ReportGenerator.formatReportAsText(report, isArabic)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                putExtra(Intent.EXTRA_SUBJECT, "Security Audit: ${report.targetIp}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Text Report"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("share_text_report_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isArabic) "مشاركة نص" else "Share Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val reportsDir = File(context.getExternalFilesDir(null), "reports")
                                if (!reportsDir.exists()) reportsDir.mkdirs()
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                val csvFile = File(reportsDir, "nslab_report_$timestamp.csv")
                                csvFile.writeText(ReportGenerator.formatReportAsCsv(report))

                                Toast.makeText(
                                    context,
                                    if (isArabic) "تم حفظ CSV في: ${csvFile.name}" else "Saved CSV: ${csvFile.name}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("export_csv_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberOutline)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isArabic) "حفظ CSV" else "Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Findings List
            Text(
                text = "${if (isArabic) "الملاحظات وثغرات CVE المكتشفة" else "Audit Findings & CVE Vulnerabilities"} (${report.findings.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (report.findings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isArabic) "لم يتم رصد أي مخاطر واضحة على المنافذ المفحوصة" else "No major risks identified on scanned endpoints",
                            color = CyberSuccess,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                report.findings.forEach { finding ->
                    FindingCard(finding = finding, isArabic = isArabic)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FindingCard(finding: SecurityFinding, isArabic: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finding_card_${finding.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when (finding.severity) {
                    FindingSeverity.CRITICAL, FindingSeverity.HIGH -> CyberDanger.copy(alpha = 0.5f)
                    FindingSeverity.MEDIUM -> CyberWarning.copy(alpha = 0.5f)
                    else -> CyberOutline
                }
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) finding.titleAr else finding.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CyberTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SeverityBadge(severity = finding.severity, isArabic = isArabic)
            }

            // CVE and CVSS tags
            if (finding.cve != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(text = finding.cve.cveId, color = CyberPrimary)
                    finding.cvssScore?.let { cvss ->
                        StatusBadge(
                            text = "CVSS $cvss",
                            color = if (cvss >= 9.0f) CyberDanger else if (cvss >= 7.0f) CyberWarning else CyberInfo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isArabic) finding.descriptionAr else finding.description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = CyberTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recommendation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant)
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = if (isArabic) "التوصية الأمنية للإصلاح:" else "Security Recommendation:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic) finding.recommendationAr else finding.recommendation,
                        fontSize = 12.sp,
                        color = CyberTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityCountBox(label: String, count: Int, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 11.sp, color = CyberTextSecondary)
        Text(text = "$count", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
