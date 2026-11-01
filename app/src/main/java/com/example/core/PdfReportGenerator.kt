package com.example.core

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.AuditReport
import com.example.data.FindingSeverity
import com.example.data.PortState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generatePdfReport(context: Context, report: AuditReport, isArabic: Boolean): File {
        val pdfDoc = PdfDocument()

        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points

        // Paints
        val paintText = Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = Color.rgb(20, 24, 33)
        }

        val paintBold = Paint().apply {
            isAntiAlias = true
            textSize = 11f
            isFakeBoldText = true
            color = Color.rgb(15, 23, 42)
        }

        val paintTitle = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            isFakeBoldText = true
            color = Color.WHITE
        }

        val paintBox = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val paintStroke = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
            color = Color.rgb(203, 213, 225)
        }

        // Page 1
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        // Header Background
        paintBox.color = Color.rgb(15, 23, 42) // Dark Navy Cyber
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paintBox)

        // Header Title
        canvas.drawText(
            if (isArabic) "مختبر أمان الشبكات — تقرير الفحص والتدقيق الأمني" else "Network Security Lab — Security Audit Report",
            30f, 40f, paintTitle
        )

        paintText.color = Color.rgb(148, 163, 184)
        paintText.textSize = 10f
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(report.generatedAtMillis))
        canvas.drawText(
            "Report ID: ${report.id}  •  Generated: $dateStr  •  Target: ${report.targetIp}",
            30f, 65f, paintText
        )

        var y = 115f

        // Overview / Posture Card
        val cardRect = RectF(30f, y, (pageWidth - 30).toFloat(), y + 85f)
        paintBox.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(cardRect, 8f, 8f, paintBox)
        canvas.drawRoundRect(cardRect, 8f, 8f, paintStroke)

        // Score Ring / Gauge
        val score = report.score
        val scoreColor = when {
            score >= 80 -> Color.rgb(34, 197, 94) // Green
            score >= 60 -> Color.rgb(245, 158, 11) // Amber
            else -> Color.rgb(239, 68, 68) // Red
        }

        val circlePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
            color = scoreColor
        }
        val circleBg = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = Color.rgb(241, 245, 249)
        }

        canvas.drawCircle(80f, y + 42f, 28f, circleBg)
        canvas.drawCircle(80f, y + 42f, 28f, circlePaint)

        val scoreTextPaint = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            isFakeBoldText = true
            color = scoreColor
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$score", 80f, y + 48f, scoreTextPaint)

        // Metadata alongside score
        paintBold.textSize = 13f
        canvas.drawText(
            if (isArabic) "مؤشر الوضع الأمني العام (Security Posture)" else "Overall Security Posture Score",
            125f, y + 30f, paintBold
        )

        paintText.color = Color.rgb(71, 85, 105)
        paintText.textSize = 10f
        canvas.drawText(
            "Target Network: ${report.targetNetwork}  •  Hosts Discovered on LAN: ${report.devicesCount}",
            125f, y + 50f, paintText
        )

        val openCount = report.scannedPorts.count { it.state == PortState.OPEN }
        val criticalHighCount = report.findings.count { it.severity == FindingSeverity.CRITICAL || it.severity == FindingSeverity.HIGH }
        canvas.drawText(
            "Open Ports: $openCount  •  High/Critical CVE Risks: $criticalHighCount",
            125f, y + 66f, paintText
        )

        y += 105f

        // Section: Open Ports Table
        paintBold.textSize = 12f
        paintBold.color = Color.rgb(15, 23, 42)
        canvas.drawText(
            if (isArabic) "المنافذ المفتوحة والخدمات المكتشفة (Open Ports & Discovered Services)" else "Open Ports & Discovered Services",
            30f, y, paintBold
        )
        y += 12f

        // Table Header
        paintBox.color = Color.rgb(226, 232, 240)
        canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 20f, paintBox)

        paintBold.textSize = 9f
        canvas.drawText("PORT", 40f, y + 14f, paintBold)
        canvas.drawText("SERVICE", 100f, y + 14f, paintBold)
        canvas.drawText("STATE", 240f, y + 14f, paintBold)
        canvas.drawText("LATENCY", 320f, y + 14f, paintBold)
        canvas.drawText("CVE MATCHES", 410f, y + 14f, paintBold)
        y += 20f

        val openPorts = report.scannedPorts.filter { it.state == PortState.OPEN }.take(8)
        paintText.textSize = 9f
        paintText.color = Color.rgb(15, 23, 42)

        for (port in openPorts) {
            paintBox.color = if (port.port % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
            canvas.drawRect(30f, y, (pageWidth - 30).toFloat(), y + 18f, paintBox)

            canvas.drawText("${port.port}/TCP", 40f, y + 13f, paintText)
            canvas.drawText(port.serviceName.take(24), 100f, y + 13f, paintText)

            val portStatusPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9f
                isFakeBoldText = true
                color = Color.rgb(22, 163, 74)
            }
            canvas.drawText("OPEN", 240f, y + 13f, portStatusPaint)
            canvas.drawText("${port.responseTimeMs} ms", 320f, y + 13f, paintText)

            val cveSummary = if (port.matchedCves.isNotEmpty()) {
                "${port.matchedCves.size} CVEs (Max CVSS ${port.matchedCves.maxOfOrNull { it.cvssScore }})"
            } else {
                "None"
            }
            canvas.drawText(cveSummary, 410f, y + 13f, paintText)

            y += 18f
        }

        if (openPorts.isEmpty()) {
            canvas.drawText("No open ports found on target host.", 40f, y + 14f, paintText)
            y += 18f
        }

        y += 20f

        // Section: Identified Vulnerabilities & Remediation
        paintBold.textSize = 12f
        canvas.drawText(
            if (isArabic) "المخاطر المكتشفة وتوصيات المعالجة (Vulnerabilities & Remediation)" else "Vulnerabilities & Remediation Playbooks",
            30f, y, paintBold
        )
        y += 14f

        for (finding in report.findings.take(4)) {
            val fBox = RectF(30f, y, (pageWidth - 30).toFloat(), y + 68f)
            paintBox.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(fBox, 6f, 6f, paintBox)

            val fBorder = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
                color = when (finding.severity) {
                    FindingSeverity.CRITICAL, FindingSeverity.HIGH -> Color.rgb(239, 68, 68)
                    FindingSeverity.MEDIUM -> Color.rgb(245, 158, 11)
                    else -> Color.rgb(203, 213, 225)
                }
            }
            canvas.drawRoundRect(fBox, 6f, 6f, fBorder)

            // Finding Title & Badge
            paintBold.textSize = 10f
            paintBold.color = Color.rgb(15, 23, 42)
            val title = (if (isArabic) finding.titleAr else finding.title).take(65)
            canvas.drawText(title, 40f, y + 18f, paintBold)

            paintText.textSize = 8.5f
            paintText.color = Color.rgb(71, 85, 105)
            val desc = (if (isArabic) finding.descriptionAr else finding.description).take(95)
            canvas.drawText(desc, 40f, y + 34f, paintText)

            // Remediation
            val remPaint = Paint().apply {
                isAntiAlias = true
                textSize = 8.5f
                isFakeBoldText = true
                color = Color.rgb(37, 99, 235)
            }
            val rem = "Remediation: " + (if (isArabic) finding.recommendationAr else finding.recommendation).take(85)
            canvas.drawText(rem, 40f, y + 52f, remPaint)

            y += 76f
        }

        // Footer
        paintText.textSize = 8f
        paintText.color = Color.rgb(148, 163, 184)
        canvas.drawText(
            "Network Security Lab • Strictly for Authorized Defensive Diagnostics • Generated Locally on Android",
            30f, pageHeight - 20f, paintText
        )

        pdfDoc.finishPage(page)

        // Save PDF to reports directory
        val reportsDir = File(context.getExternalFilesDir(null), "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val pdfFile = File(reportsDir, "nslab_audit_$timestamp.pdf")

        FileOutputStream(pdfFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return pdfFile
    }

    fun sharePdfReport(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Network Security Audit: ${pdfFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Security Audit PDF"))
    }
}
