package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.CveKnowledgeBase
import com.example.core.DeviceDiscovery
import com.example.core.DnsTester
import com.example.core.HttpAnalyzer
import com.example.core.NetworkInfoProvider
import com.example.core.NetworkWatchdogJobService
import com.example.core.PdfReportGenerator
import com.example.core.PingTester
import com.example.core.PortScanner
import com.example.core.ServiceDetector
import com.example.core.TracerouteTester
import com.example.data.AppPreferences
import com.example.data.AuditReport
import com.example.data.DiscoveredDevice
import com.example.data.DnsInspectionResult
import com.example.data.HttpInspectionResult
import com.example.data.LocalStorage
import com.example.data.NetworkInfoData
import com.example.data.PingResult
import com.example.data.PortScanItem
import com.example.data.PortState
import com.example.data.ReportGenerator
import com.example.data.TlsCertInfo
import com.example.data.TracerouteHop
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    NETWORK_INFO,
    DEVICE_DISCOVERY,
    PORT_SCAN,
    HTTP_ANALYZER,
    TOOLS,
    REPORT,
    ADVANCED,
    SETTINGS
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.NETWORK_INFO,
    val networkInfo: NetworkInfoData = NetworkInfoData(),
    val preferences: AppPreferences = AppPreferences(),

    // Devices
    val discoveredDevices: List<DiscoveredDevice> = emptyList(),
    val isDiscovering: Boolean = false,
    val discoveryProgress: Pair<Int, Int> = Pair(0, 0),

    // Ports
    val targetPortIp: String = "127.0.0.1",
    val portScanResults: List<PortScanItem> = emptyList(),
    val isScanningPorts: Boolean = false,
    val portScanProgress: Pair<Int, Int> = Pair(0, 0),
    val inspectedPortBanner: String? = null,
    val inspectedPortTls: TlsCertInfo? = null,
    val isInspectingService: Boolean = false,

    // HTTP
    val httpResult: HttpInspectionResult? = null,
    val isAnalyzingHttp: Boolean = false,

    // Tools
    val dnsResult: DnsInspectionResult? = null,
    val isTestingDns: Boolean = false,
    val pingResult: PingResult? = null,
    val isPinging: Boolean = false,
    val tracerouteHops: List<TracerouteHop> = emptyList(),
    val isTracing: Boolean = false,

    // Report
    val currentReport: AuditReport? = null,
    val isGeneratingPdf: Boolean = false,

    // Status message for Snackbars
    val snackbarMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val localStorage = LocalStorage(application)
    private val networkInfoProvider = NetworkInfoProvider(application)
    private val deviceDiscovery = DeviceDiscovery()
    private val portScanner = PortScanner()
    private val serviceDetector = ServiceDetector()
    private val httpAnalyzer = HttpAnalyzer()
    private val dnsTester = DnsTester()
    private val pingTester = PingTester()
    private val tracerouteTester = TracerouteTester()

    private val _uiState = MutableStateFlow(UiState(preferences = localStorage.getPreferences()))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var discoveryJob: Job? = null
    private var portScanJob: Job? = null
    private var pingJob: Job? = null
    private var traceJob: Job? = null

    init {
        refreshNetworkInfo()
        // If watchdog is enabled, ensure job is scheduled
        if (localStorage.isWatchdogEnabled()) {
            NetworkWatchdogJobService.scheduleWatchdog(
                application,
                localStorage.getWatchdogIntervalMinutes()
            )
        }
    }

    fun setScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun acceptDisclaimer() {
        localStorage.setDisclaimerAccepted(true)
        _uiState.update {
            it.copy(preferences = it.preferences.copy(disclaimerAccepted = true))
        }
    }

    fun toggleLanguage() {
        val newLang = !_uiState.value.preferences.isArabic
        localStorage.setArabic(newLang)
        _uiState.update {
            it.copy(preferences = it.preferences.copy(isArabic = newLang))
        }
    }

    fun setLocalLabMode(enabled: Boolean) {
        localStorage.setLocalLabMode(enabled)
        _uiState.update {
            it.copy(preferences = it.preferences.copy(localLabMode = enabled))
        }
    }

    fun setTimeoutMs(timeout: Int) {
        localStorage.setTimeoutMs(timeout)
        _uiState.update {
            it.copy(preferences = it.preferences.copy(timeoutMs = timeout))
        }
    }

    fun setDarkMode(isDark: Boolean) {
        localStorage.setDarkMode(isDark)
        _uiState.update {
            it.copy(preferences = it.preferences.copy(isDarkMode = isDark))
        }
    }

    fun setWatchdogEnabled(enabled: Boolean) {
        localStorage.setWatchdogEnabled(enabled)
        val interval = localStorage.getWatchdogIntervalMinutes()
        if (enabled) {
            NetworkWatchdogJobService.scheduleWatchdog(getApplication(), interval)
        } else {
            NetworkWatchdogJobService.cancelWatchdog(getApplication())
        }
        _uiState.update {
            it.copy(preferences = it.preferences.copy(watchdogEnabled = enabled))
        }
        showSnackbar(
            if (_uiState.value.preferences.isArabic)
                if (enabled) "تم تفعيل حارس الشبكة في الخلفية" else "تم إيقاف حارس الشبكة"
            else
                if (enabled) "Network Watchdog active in background" else "Network Watchdog disabled"
        )
    }

    fun setWatchdogInterval(minutes: Int) {
        localStorage.setWatchdogIntervalMinutes(minutes)
        if (localStorage.isWatchdogEnabled()) {
            NetworkWatchdogJobService.scheduleWatchdog(getApplication(), minutes)
        }
        _uiState.update {
            it.copy(preferences = it.preferences.copy(watchdogIntervalMinutes = minutes))
        }
    }

    fun trustCurrentDevices() {
        val currentMacs = _uiState.value.discoveredDevices.map { it.macAddress.uppercase() }
            .filter { it != "UNKNOWN" && it != "DEVICE HOST" }.toSet()
        val allTrusted = localStorage.getKnownDeviceMacs() + currentMacs
        localStorage.setKnownDeviceMacs(allTrusted)
        _uiState.update {
            it.copy(preferences = it.preferences.copy(knownDeviceMacs = allTrusted))
        }
        showSnackbar(
            if (_uiState.value.preferences.isArabic)
                "تم توثيق ${currentMacs.size} جهاز كأجهزة موثوقة"
            else
                "Marked ${currentMacs.size} devices as trusted"
        )
    }

    fun setTargetPortIp(ip: String) {
        _uiState.update { it.copy(targetPortIp = ip) }
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch {
            val info = networkInfoProvider.getNetworkInfo()
            _uiState.update {
                it.copy(
                    networkInfo = info,
                    targetPortIp = if (it.targetPortIp == "127.0.0.1" && info.localIp != "127.0.0.1") info.localIp else it.targetPortIp
                )
            }
        }
    }

    fun startDeviceDiscovery() {
        discoveryJob?.cancel()
        _uiState.update {
            it.copy(
                isDiscovering = true,
                discoveredDevices = emptyList(),
                discoveryProgress = Pair(0, 1)
            )
        }

        discoveryJob = viewModelScope.launch {
            val net = _uiState.value.networkInfo
            val isLab = _uiState.value.preferences.localLabMode
            val results = deviceDiscovery.discoverDevices(
                localIp = net.localIp,
                gatewayIp = net.gatewayIp,
                isLabMode = isLab,
                onProgress = { scanned, total, currentDevice ->
                    _uiState.update { state ->
                        val updatedList = if (currentDevice != null && state.discoveredDevices.none { it.ip == currentDevice.ip }) {
                            state.discoveredDevices + currentDevice
                        } else {
                            state.discoveredDevices
                        }
                        state.copy(
                            discoveredDevices = updatedList,
                            discoveryProgress = Pair(scanned, total)
                        )
                    }
                }
            )
            _uiState.update {
                it.copy(
                    discoveredDevices = results,
                    isDiscovering = false,
                    snackbarMessage = if (it.preferences.isArabic)
                        "اكتمل اكتشاف الشبكة: عُثر على ${results.size} جهاز"
                    else
                        "Discovery complete: Found ${results.size} devices"
                )
            }
        }
    }

    fun stopDeviceDiscovery() {
        discoveryJob?.cancel()
        _uiState.update { it.copy(isDiscovering = false) }
    }

    fun startPortScan(targetIp: String, ports: List<Int>) {
        portScanJob?.cancel()
        _uiState.update {
            it.copy(
                targetPortIp = targetIp,
                portScanResults = emptyList(),
                isScanningPorts = true,
                portScanProgress = Pair(0, ports.size),
                inspectedPortBanner = null,
                inspectedPortTls = null
            )
        }

        portScanJob = viewModelScope.launch {
            val timeout = _uiState.value.preferences.timeoutMs
            val results = portScanner.scanPorts(
                targetIp = targetIp,
                portsToScan = ports,
                timeoutMs = timeout,
                onProgress = { scanned, total, item ->
                    val matchedCves = if (item.state == PortState.OPEN) {
                        CveKnowledgeBase.findMatchesForPort(item.port, item.banner)
                    } else emptyList()
                    val enrichedItem = item.copy(matchedCves = matchedCves)

                    _uiState.update { state ->
                        state.copy(
                            portScanResults = state.portScanResults + enrichedItem,
                            portScanProgress = Pair(scanned, total)
                        )
                    }
                }
            )
            _uiState.update {
                it.copy(
                    portScanResults = results,
                    isScanningPorts = false,
                    snackbarMessage = if (it.preferences.isArabic)
                        "اكتمل فحص المنافذ لـ $targetIp (${results.count { r -> r.state == PortState.OPEN }} مفتوح)"
                    else
                        "Port scan complete for $targetIp (${results.count { r -> r.state == PortState.OPEN }} open)"
                )
            }
            generateReport()
        }
    }

    fun stopPortScan() {
        portScanJob?.cancel()
        _uiState.update { it.copy(isScanningPorts = false) }
    }

    fun inspectPortService(ip: String, port: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInspectingService = true,
                    inspectedPortBanner = null,
                    inspectedPortTls = null
                )
            }
            val timeout = _uiState.value.preferences.timeoutMs
            val banner = serviceDetector.grabBanner(ip, port, timeout)
            val tls = if (port == 443 || port == 8443) {
                serviceDetector.inspectTlsCertificate(ip, port, timeout + 500)
            } else null

            // Update matched CVEs with the banner
            val matchedCves = CveKnowledgeBase.findMatchesForPort(port, banner)
            _uiState.update { state ->
                val updatedPorts = state.portScanResults.map { p ->
                    if (p.port == port) p.copy(banner = banner, matchedCves = matchedCves) else p
                }
                state.copy(
                    isInspectingService = false,
                    inspectedPortBanner = banner ?: if (state.preferences.isArabic) "لم يتم إرجاع أي رد من الخدمة" else "No service banner returned",
                    inspectedPortTls = tls,
                    portScanResults = updatedPorts
                )
            }
            generateReport()
        }
    }

    fun analyzeHttp(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingHttp = true, httpResult = null) }
            val timeout = _uiState.value.preferences.timeoutMs + 1000
            val result = httpAnalyzer.analyze(url, timeout)
            _uiState.update {
                it.copy(isAnalyzingHttp = false, httpResult = result)
            }
            generateReport()
        }
    }

    fun testDns(domain: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingDns = true, dnsResult = null) }
            val dnsList = _uiState.value.networkInfo.dnsServers
            val result = dnsTester.resolve(domain, dnsList)
            _uiState.update {
                it.copy(isTestingDns = false, dnsResult = result)
            }
        }
    }

    fun runPing(host: String) {
        pingJob?.cancel()
        _uiState.update { it.copy(isPinging = true, pingResult = null) }
        pingJob = viewModelScope.launch {
            val result = pingTester.ping(host, 4)
            _uiState.update {
                it.copy(isPinging = false, pingResult = result)
            }
        }
    }

    fun runTraceroute(host: String) {
        traceJob?.cancel()
        _uiState.update { it.copy(isTracing = true, tracerouteHops = emptyList()) }
        traceJob = viewModelScope.launch {
            val hops = tracerouteTester.trace(host, 15) { hop ->
                _uiState.update { state ->
                    state.copy(tracerouteHops = state.tracerouteHops + hop)
                }
            }
            _uiState.update {
                it.copy(isTracing = false, tracerouteHops = hops)
            }
        }
    }

    fun generateReport() {
        val state = _uiState.value
        val report = ReportGenerator.generateAuditReport(
            targetIp = state.targetPortIp,
            targetNetwork = state.networkInfo.networkCidr,
            scannedPorts = state.portScanResults,
            discoveredDevices = state.discoveredDevices,
            httpResults = listOfNotNull(state.httpResult)
        )
        _uiState.update { it.copy(currentReport = report) }
    }

    fun exportAndSharePdf(context: Context) {
        val report = _uiState.value.currentReport ?: return
        val isArabic = _uiState.value.preferences.isArabic

        _uiState.update { it.copy(isGeneratingPdf = true) }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val pdfFile = PdfReportGenerator.generatePdfReport(context, report, isArabic)
                _uiState.update { it.copy(isGeneratingPdf = false) }
                PdfReportGenerator.sharePdfReport(context, pdfFile)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        snackbarMessage = "PDF Error: ${e.message}"
                    )
                }
            }
        }
    }
}
