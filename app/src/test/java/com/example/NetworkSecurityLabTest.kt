package com.example

import com.example.core.CveKnowledgeBase
import com.example.core.NetworkUtils
import com.example.data.DiscoveredDevice
import com.example.data.FindingSeverity
import com.example.data.PortScanItem
import com.example.data.PortState
import com.example.data.ReportGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSecurityLabTest {

    @Test
    fun testIpConversions() {
        val ip = "192.168.1.1"
        val ipInt = NetworkUtils.ipToInt(ip)
        val convertedBack = NetworkUtils.intToIp(ipInt)
        assertEquals(ip, convertedBack)
    }

    @Test
    fun testSubnetHostsCalculation() {
        val hosts = NetworkUtils.calculateSubnetHosts("192.168.1.50", 24)
        assertEquals(254, hosts.size)
        assertTrue(hosts.contains("192.168.1.1"))
        assertTrue(hosts.contains("192.168.1.254"))
    }

    @Test
    fun testKnownVendorLookup() {
        val vendor = NetworkUtils.lookupVendor("B8:27:EB:12:34:56")
        assertEquals("Raspberry Pi", vendor)

        val vmware = NetworkUtils.lookupVendor("00:50:56:AA:BB:CC")
        assertEquals("VMware", vmware)
    }

    @Test
    fun testCveMatching() {
        // Telnet matching
        val telnetCves = CveKnowledgeBase.findMatchesForPort(23, null)
        assertTrue(telnetCves.isNotEmpty())
        assertTrue(telnetCves.any { it.cveId == "CVE-2020-10188" })

        // SMB matching
        val smbCves = CveKnowledgeBase.findMatchesForPort(445, null)
        assertTrue(smbCves.any { it.cveId == "CVE-2017-0144" })

        // Banner specific matching
        val romPagerCves = CveKnowledgeBase.findMatchesForPort(80, "Server: RomPager/4.07")
        assertTrue(romPagerCves.any { it.cveId == "CVE-2014-9222" })
    }

    @Test
    fun testSecurityReportAuditWithCves() {
        val ports = listOf(
            PortScanItem(23, "Telnet", PortState.OPEN, 15),
            PortScanItem(445, "SMB", PortState.OPEN, 20),
            PortScanItem(443, "HTTPS", PortState.OPEN, 25)
        )
        val devices = listOf(
            DiscoveredDevice(ip = "192.168.1.1", isGateway = true)
        )

        val report = ReportGenerator.generateAuditReport(
            targetIp = "192.168.1.1",
            targetNetwork = "192.168.1.0/24",
            scannedPorts = ports,
            discoveredDevices = devices
        )

        // Should contain CVE findings with CVSS scores
        val cveFindings = report.findings.filter { it.cve != null }
        assertTrue(cveFindings.isNotEmpty())
        assertTrue(report.score < 100)
    }
}
