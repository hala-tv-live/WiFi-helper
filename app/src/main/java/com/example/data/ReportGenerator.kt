package com.example.data

import com.example.core.CveKnowledgeBase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ReportGenerator {

    fun generateAuditReport(
        targetIp: String,
        targetNetwork: String,
        scannedPorts: List<PortScanItem>,
        discoveredDevices: List<DiscoveredDevice>,
        httpResults: List<HttpInspectionResult> = emptyList()
    ): AuditReport {
        val findings = mutableListOf<SecurityFinding>()
        var score = 100

        val openPorts = scannedPorts.filter { it.state == PortState.OPEN }

        for (item in openPorts) {
            val cveMatches = CveKnowledgeBase.findMatchesForPort(item.port, item.banner)
            for (cve in cveMatches) {
                val sev = when {
                    cve.cvssScore >= 9.0f -> FindingSeverity.CRITICAL
                    cve.cvssScore >= 7.0f -> FindingSeverity.HIGH
                    cve.cvssScore >= 4.0f -> FindingSeverity.MEDIUM
                    else -> FindingSeverity.LOW
                }
                score -= when (sev) {
                    FindingSeverity.CRITICAL -> 25
                    FindingSeverity.HIGH -> 15
                    FindingSeverity.MEDIUM -> 8
                    else -> 4
                }

                findings.add(
                    SecurityFinding(
                        id = UUID.randomUUID().toString(),
                        title = "${cve.cveId} (CVSS ${cve.cvssScore}): ${cve.titleEn}",
                        titleAr = "${cve.cveId} (درجة ${cve.cvssScore}): ${cve.titleAr}",
                        severity = sev,
                        port = item.port,
                        target = targetIp,
                        description = cve.descriptionEn,
                        descriptionAr = cve.descriptionAr,
                        recommendation = cve.remediationEn,
                        recommendationAr = cve.remediationAr,
                        cve = cve,
                        cvssScore = cve.cvssScore
                    )
                )
            }

            // General heuristic findings if no specific CVE matched
            if (cveMatches.isEmpty()) {
                findings.add(
                    SecurityFinding(
                        id = UUID.randomUUID().toString(),
                        title = "Info: Service Port ${item.port} Active",
                        titleAr = "معلومة: خدمة المنفذ ${item.port} نشطة",
                        severity = FindingSeverity.INFO,
                        port = item.port,
                        target = targetIp,
                        description = "Service '${item.serviceName}' is reachable on target.",
                        descriptionAr = "الخدمة '${item.serviceName}' متاحة على الجهاز المستهدف.",
                        recommendation = "Verify if this service is required for business operations.",
                        recommendationAr = "تحقق مما إذا كانت هذه الخدمة ضرورية بالفعل في شبكتك."
                    )
                )
            }
        }

        // Evaluate HTTP security header findings
        for (http in httpResults) {
            for (missing in http.securityHeadersMissing) {
                if (missing == "Strict-Transport-Security" && http.isHttps) {
                    score -= 5
                    findings.add(
                        SecurityFinding(
                            id = UUID.randomUUID().toString(),
                            title = "Medium Risk: Missing HSTS Header on Web Server",
                            titleAr = "خطر متوسط: غياب ترويسة HSTS المشفرة",
                            severity = FindingSeverity.MEDIUM,
                            target = http.url,
                            description = "Without HTTP Strict Transport Security, browsers may be downgraded to unencrypted HTTP.",
                            descriptionAr = "بدون ترويسة HSTS يمكن تخفيض اتصال المتصفح إلى بروتوكول HTTP غير المشفر.",
                            recommendation = "Add 'Strict-Transport-Security: max-age=31536000; includeSubDomains' response header.",
                            recommendationAr = "أضف ترويسة Strict-Transport-Security في إعدادات خادم الويب."
                        )
                    )
                }
            }
        }

        val clampedScore = score.coerceIn(10, 100)

        // Enrich scannedPorts with matched CVEs
        val enrichedPorts = scannedPorts.map { p ->
            if (p.state == PortState.OPEN) {
                p.copy(matchedCves = CveKnowledgeBase.findMatchesForPort(p.port, p.banner))
            } else p
        }

        return AuditReport(
            id = "RPT-" + UUID.randomUUID().toString().take(8).uppercase(),
            generatedAtMillis = System.currentTimeMillis(),
            targetNetwork = targetNetwork,
            targetIp = targetIp,
            score = clampedScore,
            findings = findings.sortedBy { it.severity.ordinal },
            scannedPorts = enrichedPorts,
            devicesCount = discoveredDevices.size
        )
    }

    fun formatReportAsText(report: AuditReport, isArabic: Boolean): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(report.generatedAtMillis))
        val sb = StringBuilder()

        if (isArabic) {
            sb.appendLine("═════════════════════════════════════════════════════")
            sb.appendLine("  تقرير الفحص والتدقيق الأمني — Network Security Lab")
            sb.appendLine("═════════════════════════════════════════════════════")
            sb.appendLine("معرف التقرير: ${report.id}")
            sb.appendLine("تاريخ التوليد: $dateStr")
            sb.appendLine("الشبكة المستهدفة: ${report.targetNetwork}")
            sb.appendLine("الجهاز المستهدف: ${report.targetIp}")
            sb.appendLine("عدد الأجهزة المكتشفة: ${report.devicesCount}")
            sb.appendLine("تقييم الأمان الإجمالي: ${report.score} / 100")
            sb.appendLine("-----------------------------------------------------")
            sb.appendLine("المنافذ المفتوحة (${report.scannedPorts.count { it.state == PortState.OPEN }}):")
            report.scannedPorts.filter { it.state == PortState.OPEN }.forEach {
                sb.appendLine("  - [منفذ ${it.port}] ${it.serviceName} (${it.responseTimeMs} ms)")
                it.matchedCves.forEach { cve ->
                    sb.appendLine("      ↳ ثغرة: ${cve.cveId} (درجة CVSS: ${cve.cvssScore})")
                }
            }
            sb.appendLine("-----------------------------------------------------")
            sb.appendLine("الملاحظات والتوصيات الأمنية (${report.findings.size}):")
            report.findings.forEachIndexed { index, f ->
                sb.appendLine("\n[${index + 1}] [${f.severity.name}] ${f.titleAr}")
                if (f.cve != null) {
                    sb.appendLine("    معرف الثغرة: ${f.cve.cveId} (درجة خطورة CVSS: ${f.cvssScore})")
                }
                sb.appendLine("    الوصف: ${f.descriptionAr}")
                sb.appendLine("    التوصية: ${f.recommendationAr}")
            }
            sb.appendLine("\n═════════════════════════════════════════════════════")
            sb.appendLine("تنبيه: هذا التقرير مخصص للأغراض التشخيصية والدفاعية فقط.")
            sb.appendLine("═════════════════════════════════════════════════════")
        } else {
            sb.appendLine("=====================================================")
            sb.appendLine("  Network Security Audit Report — Network Security Lab")
            sb.appendLine("=====================================================")
            sb.appendLine("Report ID: ${report.id}")
            sb.appendLine("Generated At: $dateStr")
            sb.appendLine("Target Network: ${report.targetNetwork}")
            sb.appendLine("Target Host: ${report.targetIp}")
            sb.appendLine("Discovered Devices: ${report.devicesCount}")
            sb.appendLine("Overall Security Score: ${report.score} / 100")
            sb.appendLine("-----------------------------------------------------")
            sb.appendLine("Open Ports (${report.scannedPorts.count { it.state == PortState.OPEN }}):")
            report.scannedPorts.filter { it.state == PortState.OPEN }.forEach {
                sb.appendLine("  - [Port ${it.port}] ${it.serviceName} (${it.responseTimeMs} ms)")
                it.matchedCves.forEach { cve ->
                    sb.appendLine("      ↳ CVE: ${cve.cveId} (CVSS: ${cve.cvssScore})")
                }
            }
            sb.appendLine("-----------------------------------------------------")
            sb.appendLine("Security Findings & Recommendations (${report.findings.size}):")
            report.findings.forEachIndexed { index, f ->
                sb.appendLine("\n[${index + 1}] [${f.severity.name}] ${f.title}")
                if (f.cve != null) {
                    sb.appendLine("    Vulnerability: ${f.cve.cveId} (CVSS: ${f.cvssScore})")
                }
                sb.appendLine("    Description: ${f.description}")
                sb.appendLine("    Recommendation: ${f.recommendation}")
            }
            sb.appendLine("\n=====================================================")
            sb.appendLine("Notice: Strictly for defensive diagnostics and authorized testing.")
            sb.appendLine("=====================================================")
        }

        return sb.toString()
    }

    fun formatReportAsCsv(report: AuditReport): String {
        val sb = StringBuilder()
        sb.appendLine("Type,Target,Port,CVE_ID,CVSS,Severity,Title,Description,Recommendation")
        for (f in report.findings) {
            val cveId = f.cve?.cveId ?: ""
            val cvss = f.cvssScore ?: ""
            val titleEsc = "\"${f.title.replace("\"", "\"\"")}\""
            val descEsc = "\"${f.description.replace("\"", "\"\"")}\""
            val recEsc = "\"${f.recommendation.replace("\"", "\"\"")}\""
            sb.appendLine("Finding,${f.target},${f.port ?: 0},$cveId,$cvss,${f.severity.name},$titleEsc,$descEsc,$recEsc")
        }
        for (p in report.scannedPorts) {
            sb.appendLine("PortScan,${report.targetIp},${p.port},\"\",,${p.state.name},\"${p.serviceName}\",\"Latency ${p.responseTimeMs}ms\",\"\"")
        }
        return sb.toString()
    }
}
