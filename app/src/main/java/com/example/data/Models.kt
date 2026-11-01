package com.example.data

enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OFFLINE
}

data class NetworkInfoData(
    val localIp: String = "0.0.0.0",
    val gatewayIp: String = "0.0.0.0",
    val subnetMask: String = "255.255.255.0",
    val networkCidr: String = "0.0.0.0/24",
    val dnsServers: List<String> = emptyList(),
    val connectionType: ConnectionType = ConnectionType.OFFLINE,
    val ssid: String = "Not Connected",
    val bssid: String = "00:00:00:00:00:00",
    val interfaceName: String = "wlan0",
    val macAddress: String = "Unknown",
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0
)

data class DiscoveredDevice(
    val ip: String,
    val macAddress: String = "Unknown",
    val hostname: String = "Unknown",
    val vendor: String = "Generic / Unknown",
    val isGateway: Boolean = false,
    val isSelf: Boolean = false,
    val responseTimeMs: Long = 0,
    val openPortsCount: Int = 0
)

enum class PortState {
    OPEN,
    CLOSED,
    FILTERED
}

data class PortScanItem(
    val port: Int,
    val serviceName: String,
    val state: PortState,
    val responseTimeMs: Long = 0,
    val banner: String? = null,
    val matchedCves: List<CveVulnerability> = emptyList()
)

data class TlsCertInfo(
    val subject: String = "",
    val issuer: String = "",
    val validFrom: String = "",
    val validTo: String = "",
    val serialNumber: String = "",
    val signatureAlgorithm: String = "",
    val isExpired: Boolean = false,
    val isSelfSigned: Boolean = false
)

data class HttpInspectionResult(
    val url: String,
    val responseCode: Int = 0,
    val protocol: String = "",
    val latencyMs: Long = 0,
    val contentLength: Long = 0,
    val isHttps: Boolean = false,
    val headers: Map<String, String> = emptyMap(),
    val securityHeadersFound: List<String> = emptyList(),
    val securityHeadersMissing: List<String> = emptyList(),
    val tlsCertInfo: TlsCertInfo? = null,
    val error: String? = null
)

data class DnsInspectionResult(
    val domain: String,
    val addresses: List<String> = emptyList(),
    val latencyMs: Long = 0,
    val canonicalName: String = "",
    val activeDnsServers: List<String> = emptyList(),
    val error: String? = null
)

data class PingResult(
    val host: String,
    val packetsSent: Int = 4,
    val packetsReceived: Int = 0,
    val packetLossPercent: Int = 100,
    val minRttMs: Float = 0f,
    val avgRttMs: Float = 0f,
    val maxRttMs: Float = 0f,
    val outputLog: String = "",
    val isSuccess: Boolean = false
)

data class TracerouteHop(
    val hopNumber: Int,
    val ip: String,
    val rttMs: Long,
    val isReached: Boolean
)

enum class FindingSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFO
}

data class CveVulnerability(
    val cveId: String,
    val cvssScore: Float,
    val affectedService: String,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val remediationEn: String,
    val remediationAr: String
)

data class SecurityFinding(
    val id: String,
    val title: String,
    val titleAr: String,
    val severity: FindingSeverity,
    val port: Int? = null,
    val target: String = "",
    val description: String,
    val descriptionAr: String,
    val recommendation: String,
    val recommendationAr: String,
    val cve: CveVulnerability? = null,
    val cvssScore: Float? = null
)

data class AuditReport(
    val id: String,
    val generatedAtMillis: Long,
    val targetNetwork: String,
    val targetIp: String,
    val score: Int, // 0 - 100
    val findings: List<SecurityFinding>,
    val scannedPorts: List<PortScanItem>,
    val devicesCount: Int
)

data class WatchdogAlert(
    val id: String,
    val timestamp: Long,
    val ip: String,
    val mac: String,
    val vendor: String,
    val isAcknowledged: Boolean = false
)

data class AppPreferences(
    val disclaimerAccepted: Boolean = false,
    val isArabic: Boolean = true,
    val localLabMode: Boolean = false, // limits scans to 127.0.0.1 and 10.0.2.2
    val timeoutMs: Int = 1000,
    val isDarkMode: Boolean = true,
    val watchdogEnabled: Boolean = false,
    val watchdogIntervalMinutes: Int = 15,
    val knownDeviceMacs: Set<String> = emptySet(),
    val watchdogAlerts: List<WatchdogAlert> = emptyList()
)
