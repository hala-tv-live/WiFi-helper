package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.AdvancedScreen
import com.example.ui.screens.DeviceDiscoveryScreen
import com.example.ui.screens.DisclaimerDialog
import com.example.ui.screens.HttpAnalyzerScreen
import com.example.ui.screens.NetworkInfoScreen
import com.example.ui.screens.PortScanScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.CyberOutline
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.NetSecLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            NetSecLabTheme(darkTheme = uiState.preferences.isDarkMode) {
                val layoutDirection = if (uiState.preferences.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isArabic = uiState.preferences.isArabic
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.showSnackbar(
                if (isArabic) "تم تفعيل إذن الإشعارات لحارس الشبكة" else "Notification permission granted for Watchdog"
            )
        }
    }

    // Show Snackbars when emitted
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Back handling: pop to Home screen
    if (uiState.currentScreen != AppScreen.NETWORK_INFO) {
        BackHandler {
            viewModel.setScreen(AppScreen.NETWORK_INFO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isArabic) "مختبر أمان الشبكات" else "Network Security Lab",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.setScreen(AppScreen.ADVANCED) },
                        modifier = Modifier.testTag("top_bar_advanced_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Advanced",
                            tint = if (uiState.currentScreen == AppScreen.ADVANCED) CyberPrimary else CyberTextSecondary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setScreen(AppScreen.SETTINGS) },
                        modifier = Modifier.testTag("top_bar_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (uiState.currentScreen == AppScreen.SETTINGS) CyberPrimary else CyberTextSecondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val items = listOf(
                    Triple(AppScreen.NETWORK_INFO, if (isArabic) "الشبكة" else "Network", Icons.Default.NetworkCheck),
                    Triple(AppScreen.DEVICE_DISCOVERY, if (isArabic) "الأجهزة" else "Devices", Icons.Default.Devices),
                    Triple(AppScreen.PORT_SCAN, if (isArabic) "المنافذ" else "Ports", Icons.Default.Security),
                    Triple(AppScreen.HTTP_ANALYZER, if (isArabic) "الويب" else "HTTP", Icons.Default.Http),
                    Triple(AppScreen.TOOLS, if (isArabic) "الأدوات" else "Tools", Icons.Default.Build),
                    Triple(AppScreen.REPORT, if (isArabic) "التقرير" else "Audit", Icons.Default.Assessment)
                )

                items.forEach { (screen, label, icon) ->
                    val selected = uiState.currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.setScreen(screen) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberPrimary,
                            selectedTextColor = CyberPrimary,
                            unselectedIconColor = CyberTextSecondary,
                            unselectedTextColor = CyberTextSecondary,
                            indicatorColor = CyberSurface
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                AppScreen.NETWORK_INFO -> NetworkInfoScreen(
                    networkInfo = uiState.networkInfo,
                    isArabic = isArabic,
                    onRefresh = { viewModel.refreshNetworkInfo() },
                    onToggleLang = { viewModel.toggleLanguage() },
                    onNavigate = { viewModel.setScreen(it) },
                    onScanSelf = { ip ->
                        viewModel.setTargetPortIp(ip)
                        viewModel.setScreen(AppScreen.PORT_SCAN)
                    }
                )
                AppScreen.DEVICE_DISCOVERY -> DeviceDiscoveryScreen(
                    networkInfo = uiState.networkInfo,
                    devices = uiState.discoveredDevices,
                    isDiscovering = uiState.isDiscovering,
                    progress = uiState.discoveryProgress,
                    isArabic = isArabic,
                    onStartDiscovery = { viewModel.startDeviceDiscovery() },
                    onStopDiscovery = { viewModel.stopDeviceDiscovery() },
                    onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                    onToggleLang = { viewModel.toggleLanguage() },
                    onSelectDeviceForScan = { ip ->
                        viewModel.setTargetPortIp(ip)
                        viewModel.setScreen(AppScreen.PORT_SCAN)
                    }
                )
                AppScreen.PORT_SCAN -> PortScanScreen(
                    networkInfo = uiState.networkInfo,
                    targetIp = uiState.targetPortIp,
                    results = uiState.portScanResults,
                    isScanning = uiState.isScanningPorts,
                    progress = uiState.portScanProgress,
                    inspectedBanner = uiState.inspectedPortBanner,
                    inspectedTls = uiState.inspectedPortTls,
                    isInspectingService = uiState.isInspectingService,
                    isArabic = isArabic,
                    onStartScan = { ip, ports -> viewModel.startPortScan(ip, ports) },
                    onStopScan = { viewModel.stopPortScan() },
                    onInspectPort = { ip, port -> viewModel.inspectPortService(ip, port) },
                    onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                    onToggleLang = { viewModel.toggleLanguage() }
                )
                AppScreen.HTTP_ANALYZER -> HttpAnalyzerScreen(
                    networkInfo = uiState.networkInfo,
                    result = uiState.httpResult,
                    isAnalyzing = uiState.isAnalyzingHttp,
                    isArabic = isArabic,
                    onAnalyze = { url -> viewModel.analyzeHttp(url) },
                    onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                    onToggleLang = { viewModel.toggleLanguage() }
                )
                AppScreen.TOOLS -> ToolsScreen(
                    networkInfo = uiState.networkInfo,
                    dnsResult = uiState.dnsResult,
                    isTestingDns = uiState.isTestingDns,
                    pingResult = uiState.pingResult,
                    isPinging = uiState.isPinging,
                    tracerouteHops = uiState.tracerouteHops,
                    isTracing = uiState.isTracing,
                    isArabic = isArabic,
                    onTestDns = { domain -> viewModel.testDns(domain) },
                    onRunPing = { host -> viewModel.runPing(host) },
                    onRunTraceroute = { host -> viewModel.runTraceroute(host) },
                    onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                    onToggleLang = { viewModel.toggleLanguage() }
                )
                AppScreen.REPORT -> ReportScreen(
                    networkInfo = uiState.networkInfo,
                    report = uiState.currentReport,
                    isGeneratingPdf = uiState.isGeneratingPdf,
                    isArabic = isArabic,
                    onGenerateReport = { viewModel.generateReport() },
                    onExportPdf = { viewModel.exportAndSharePdf(context) },
                    onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                    onToggleLang = { viewModel.toggleLanguage() }
                )
                AppScreen.ADVANCED -> AdvancedScreen(
                    networkInfo = uiState.networkInfo,
                    isArabic = isArabic,
                    onRefreshNetwork = { viewModel.refreshNetworkInfo() },
                    onToggleLang = { viewModel.toggleLanguage() }
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    networkInfo = uiState.networkInfo,
                    preferences = uiState.preferences,
                    isArabic = isArabic,
                    onToggleLang = { viewModel.toggleLanguage() },
                    onSetLocalLabMode = { viewModel.setLocalLabMode(it) },
                    onSetTimeout = { viewModel.setTimeoutMs(it) },
                    onSetDarkMode = { viewModel.setDarkMode(it) },
                    onSetWatchdogEnabled = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.setWatchdogEnabled(enabled)
                    },
                    onSetWatchdogInterval = { viewModel.setWatchdogInterval(it) },
                    onTrustCurrentDevices = { viewModel.trustCurrentDevices() },
                    onRefreshNetwork = { viewModel.refreshNetworkInfo() }
                )
            }

            // Mandatory First-Launch Disclaimer Modal
            if (!uiState.preferences.disclaimerAccepted) {
                DisclaimerDialog(
                    isArabic = isArabic,
                    onAccept = { viewModel.acceptDisclaimer() }
                )
            }
        }
    }
}
