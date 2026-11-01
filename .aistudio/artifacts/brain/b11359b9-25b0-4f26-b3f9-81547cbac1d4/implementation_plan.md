# Network Security Lab: Vulnerability Audit, PDF Reporting & Background Watchdog

An advanced defensive security diagnostic upgrade adding on-device CVE vulnerability matching, publication-grade PDF audit reporting with visual posture scorecards, and continuous background network monitoring with rogue/new device alert notifications.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The user confirmed the following roadmap directions during the planning interview:
> - **Primary Core Feature:** Automated vulnerability auditing with an offline/cached Common Vulnerabilities and Exposures (CVE) knowledge base matching discovered services and banner versions.
> - **Export Format:** Formatted, multi-page vector PDF reports featuring visual security grade charts, breakdown metrics, and prioritized remediation guidance.
> - **Continuous Monitoring:** Background network watchdog scans using Android `WorkManager` that trigger local notification alerts whenever an unknown/unrecognized device connects to the local subnet.

- **Confirmed Decision 1 (Vulnerability Knowledge Engine):** Use a curated on-device SQLite/Room database of high-impact network CVEs (covering routers, IoT cameras, embedded HTTP servers, SMB, SSH, FTP, Telnet) to keep scans 100% offline, privacy-preserving, and fast without sending LAN telemetry to third-party APIs.
- **Confirmed Decision 2 (PDF Generation Strategy):** Leverage Android's native `android.graphics.pdf.PdfDocument` and Canvas rendering rather than bulky third-party PDF binaries to keep the APK lightweight (< 25MB) while outputting vector graphics, colored severity badges, and Arabic/English typography.
- **Confirmed Decision 3 (Background Scheduling):** Implement battery-optimized `PeriodicWorkRequest` (15-30 minute intervals, constrained to unmetered Wi-Fi) with `NotificationChannel` alerts for newly detected MAC/IP addresses.

---

## 1. Overview & Core Concept

### What It Does
This enhancement expands **Network Security Lab** from a point-in-time diagnostic scanner into a proactive security auditing suite. When a port or device is scanned, the app parses version tokens (e.g., Apache, OpenSSH, Dropbear, busybox, Samba, GoAhead, Boa) and correlates them against known vulnerability signatures and CVE identifiers with CVSS severity ratings. Users can export branded executive PDF reports with visual radar/bar scorecards and leave a silent background watchdog running to guard home and lab networks against unauthorized rogue devices.

### Target Audience & Persona
- **Network Administrators & Security Engineers**: Requiring defensible audit artifacts and instant visibility into unauthorized devices on segmented lab or home networks.
- **Home Lab Enthusiasts & Smart Home Owners**: Needing privacy-focused, zero-cloud verification that IoT cameras, smart TVs, and routers aren't running vulnerable firmware.

### Key Value Delivered
- **Defensible Documentation**: One-tap export of clean, executive-ready PDF audit reports for compliance and proof of remediation.
- **Actionable Risk Assessment**: Converts raw open ports into clear CVE IDs, CVSS scores, and mitigation playbooks in both Arabic and English.
- **Continuous Peace of Mind**: Automatic push notifications when a new or unrecognized hardware vendor joins the local Wi-Fi.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Vulnerability Audit Flow:**
   ```
   [Device / Port Scan Results]
              │
              ▼ (Auto / Tap "Audit Vulnerabilities")
   [Service Banner Analyzer & Version Tokenizer]
              │
              ▼
   [CVE Knowledge Base Matching]
              │
              ▼
   [Vulnerability Insight Card (CVE ID, CVSS 1-10, Exploitability Context, Remediation)]
   ```

2. **Executive PDF Export Flow:**
   ```
   [Audit Report Screen]
              │
              ▼ (Tap "Generate Official PDF Report")
   [PDF Canvas Renderer (Score Gauges, Category Breakdown, CVE Matrix)]
              │
              ▼
   [Android Print / Share Intent (Open with PDF Reader or Save to Files)]
   ```

3. **Background Watchdog Flow:**
   ```
   [Settings Screen -> Toggle "Network Watchdog"]
              │
              ▼ (Configure interval: 15m / 30m / 1h)
   [WorkManager Scheduled Wi-Fi Job]
              │
              ▼ (Subnet Sweep & Compare Known Device Fingerprints)
   [New / Unknown MAC Detected?]
         ├── Yes ──► [Android Notification: "New Device Detected on LAN: 192.168.1.145 (Tuya Smart)"]
         └── No  ──► [Silent State Update]
   ```

### Visual Identity & Theme
- **Color Tokens**:
  - Background: `#0F1115` (Deep Cyber Black)
  - Surface: `#1A1D23` (High-contrast charcoal)
  - Primary Accent: `#2E7DFF` (Electric Blue)
  - Critical/High Risk: `#EF4444` (Vibrant Coral Red)
  - Medium Risk: `#F59E0B` (Amber Warning)
  - Low Risk: `#38BDF8` (Cyan Info)
  - Clean/Safe: `#22C55E` (Emerald Green)
- **Typography**: Clean monospace styling for IP/MAC and version tokens, paired with legible Arabic and English typography for remediation guides.
- **Visual Score Gauge**: Circular gauge on the Report screen and in the PDF header showing letter grades (A+, A, B, C, F) alongside numerical posture metrics.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Android `PdfDocument` vs. Third-Party PDF Library (iText/PDFBox)**
  - *Chosen Approach*: Use Android's standard `android.graphics.pdf.PdfDocument`.
  - *Why*: Eliminates large dependency bloat, preserves APK size, and provides complete Canvas drawing control for high-DPI charts, typography, and dark-mode styled layouts.
  - *Alternatives Considered*: iText has restrictive licensing (AGPL/Commercial) and adds several megabytes; Apache PDFBox on Android is notoriously slow.

- **Decision 2: Local Curated CVE Database vs. Remote NVD REST API**
  - *Chosen Approach*: Curated bundled SQLite/Room database with top high-severity network & embedded device CVEs.
  - *Why*: Complete offline reliability, instantaneous matching without network latency, and strict privacy (never sending LAN IP addresses or service banners to external cloud servers).
  - *Alternatives Considered*: Calling NVD (National Vulnerability Database) API requires API keys, rate limits, internet connectivity, and leaks network reconnaissance data.

- **Decision 3: Background Watchdog via Android `WorkManager`**
  - *Chosen Approach*: `WorkManager` with `PeriodicWorkRequestBuilder` constrained to `NetworkType.UNMETERED` (Wi-Fi).
  - *Why*: Conforms strictly to Android battery and background execution limits (Doze mode friendly), surviving device restarts.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Presentation Layer                            │
│  ┌────────────────────┐  ┌─────────────────────┐  ┌─────────────────┐  │
│  │ PortScanScreen     │  │ ReportScreen (PDF)  │  │ WatchdogSettings│  │
│  └─────────┬──────────┘  └──────────┬──────────┘  └────────┬────────┘  │
└────────────┼────────────────────────┼──────────────────────┼───────────┘
             │                        │                      │
┌────────────▼────────────────────────▼──────────────────────▼───────────┐
│                        MainViewModel & State                           │
│  • uiState: StateFlow<UiState>                                         │
│  • auditFindings: StateFlow<List<VulnerabilityFinding>>               │
│  • watchdogKnownDevices: StateFlow<Set<String>>                       │
└────────────┬────────────────────────┬──────────────────────┬───────────┘
             │                        │                      │
┌────────────▼──────────┐  ┌──────────▼──────────┐  ┌────────▼───────────┐
│ Core Vulnerability    │  │ Native PDF Engine   │  │ Background Worker  │
│ Engine                │  │                     │  │                    │
│ • Banner Tokenizer    │  │ • PdfDocument Canvas│  │ • WatchdogWorker   │
│ • CVE Signatures      │  │ • Score Ring Drawer │  │ • Subnet Sweeper   │
│ • CVSS Calculator     │  │ • FileProvider Share│  │ • NotificationMgr  │
└────────────┬──────────┘  └─────────────────────┘  └────────────────────┘
             │
┌────────────▼───────────────────────────────────────────────────────────┐
│                       Data Persistence Layer                           │
│ • Room / SQLite: Curated CVE Knowledge Base & Known MAC Fingerprints   │
│ • SharedPreferences: Watchdog Config, Intervals, Language & Theme      │
└────────────────────────────────────────────────────────────────────────┘
```

### Key Data Entities

```kotlin
data class CveVulnerability(
    val cveId: String,          // e.g., "CVE-2020-0601", "CVE-2023-48795"
    val cvssScore: Float,       // 0.0 - 10.0
    val affectedService: String,// "Dropbear", "OpenSSH", "Samba", "Telnet"
    val versionPattern: String, // Regex or semantic version boundary
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val remediationEn: String,
    val remediationAr: String
)

data class WatchdogLog(
    val timestamp: Long,
    val ip: String,
    val mac: String,
    val vendor: String,
    val isRogue: Boolean
)
```

### Implementation Sequence
1. **Module 1 (CVE Engine & Signatures):** Implement `VulnerabilityMatcher` with signature matching for SSH, Telnet, HTTP servers (Apache, lighttpd, Boa, RomPager), FTP, SMB, and RTSP with CVSS scores.
2. **Module 2 (Native PDF Engine):** Create `PdfReportGenerator` using `android.graphics.pdf.PdfDocument` with vector badges, header summaries, vulnerability details, and Arabic/English text layout.
3. **Module 3 (Background Network Watchdog):** Build `NetworkWatchdogWorker` with Android `WorkManager`, comparing subnet scan results against known device fingerprints and alerting via `NotificationManager`.
4. **Module 4 (UI Integration & Polish):** Add "Export PDF" button on `ReportScreen`, display CVE badges in `PortScanScreen`, and add Watchdog toggle in `SettingsScreen`.
