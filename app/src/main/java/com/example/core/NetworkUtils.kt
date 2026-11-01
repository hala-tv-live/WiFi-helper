package com.example.core

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.InetAddress
import java.net.NetworkInterface

object NetworkUtils {

    private val KNOWN_VENDORS = mapOf(
        "00:50:56" to "VMware",
        "08:00:27" to "VirtualBox",
        "52:54:00" to "QEMU/KVM",
        "B8:27:EB" to "Raspberry Pi",
        "DC:A6:32" to "Raspberry Pi",
        "E4:5F:01" to "Raspberry Pi",
        "00:1A:11" to "Google",
        "F4:F5:D8" to "Google",
        "3C:5A:37" to "Google",
        "00:1E:C2" to "Apple",
        "F0:18:98" to "Apple",
        "AC:DE:48" to "Apple",
        "A4:83:E7" to "Apple",
        "00:12:FB" to "Samsung",
        "8C:77:12" to "Samsung",
        "B0:72:BF" to "Samsung",
        "00:26:86" to "Cisco",
        "00:14:BF" to "Cisco",
        "F4:4B:2A" to "Cisco",
        "00:19:E0" to "TP-Link",
        "50:C7:BF" to "TP-Link",
        "D8:07:B6" to "TP-Link",
        "00:18:E7" to "Huawei",
        "70:7B:E8" to "Huawei",
        "24:0A:C4" to "Espressif (ESP32/ESP8266)",
        "30:AE:A4" to "Espressif (ESP32)",
        "00:0C:29" to "VMware",
        "00:15:5D" to "Microsoft Hyper-V",
        "44:D9:E7" to "Ubiquiti",
        "B4:FB:E4" to "Ubiquiti",
        "00:1B:21" to "Intel",
        "A4:4C:C8" to "Intel"
    )

    private val PORT_SERVICE_MAP = mapOf(
        21 to "FTP (File Transfer)",
        22 to "SSH (Secure Shell)",
        23 to "Telnet (Unencrypted CLI)",
        25 to "SMTP (Mail)",
        53 to "DNS (Domain Name Service)",
        67 to "DHCP Server",
        68 to "DHCP Client",
        80 to "HTTP (Plaintext Web)",
        110 to "POP3 (Mail)",
        123 to "NTP (Network Time)",
        135 to "MS RPC",
        139 to "NetBIOS Session",
        143 to "IMAP (Mail)",
        161 to "SNMP (Network Management)",
        443 to "HTTPS (Encrypted Web)",
        445 to "SMB (File Sharing)",
        554 to "RTSP (Streaming Video)",
        631 to "IPP (CUPS Printing)",
        993 to "IMAPS (Secure IMAP)",
        995 to "POP3S (Secure POP3)",
        1433 to "MS SQL Server",
        1521 to "Oracle DB",
        1883 to "MQTT (IoT Messaging)",
        3306 to "MySQL Database",
        3389 to "RDP (Remote Desktop)",
        5000 to "UPnP / Flask Web",
        5432 to "PostgreSQL Database",
        5900 to "VNC (Virtual Network Computing)",
        6379 to "Redis In-Memory Store",
        8000 to "HTTP-Alt / Dev Server",
        8080 to "HTTP-Proxy / Tomcat Web",
        8443 to "HTTPS-Alt / Management Web",
        8888 to "HTTP-Alt / Jupyter Web",
        9000 to "SonarQube / PHP-FPM",
        27017 to "MongoDB Database"
    )

    fun getServiceName(port: Int): String {
        return PORT_SERVICE_MAP[port] ?: "Service (Port $port)"
    }

    fun lookupVendor(mac: String?): String {
        if (mac.isNullOrBlank() || mac.equals("Unknown", ignoreCase = true)) {
            return "Generic Device"
        }
        val clean = mac.uppercase().replace("-", ":")
        if (clean.length >= 8) {
            val prefix = clean.substring(0, 8)
            KNOWN_VENDORS[prefix]?.let { return it }
        }
        return "Network Device"
    }

    fun getArpTable(): Map<String, String> {
        val arpMap = mutableMapOf<String, String>()
        try {
            val arpDoc = File("/proc/net/arp")
            if (arpDoc.exists() && arpDoc.canRead()) {
                BufferedReader(FileReader(arpDoc)).use { reader ->
                    var line: String? = reader.readLine() // Header line
                    while (reader.readLine().also { line = it } != null) {
                        val tokens = line?.trim()?.split("\\s+".toRegex()) ?: emptyList()
                        if (tokens.size >= 4) {
                            val ip = tokens[0]
                            val mac = tokens[3]
                            if (mac != "00:00:00:00:00:00" && !mac.contains("00:00:00")) {
                                arpMap[ip] = mac
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Android 10+ restricts /proc/net/arp for non-system apps
        }
        return arpMap
    }

    fun ipToInt(ip: String): Int {
        val parts = ip.split(".").map { it.toIntOrNull() ?: 0 }
        if (parts.size != 4) return 0
        return (parts[0] shl 24) or (parts[1] shl 16) or (parts[2] shl 8) or parts[3]
    }

    fun intToIp(ipInt: Int): String {
        return "${(ipInt ushr 24) and 0xFF}.${(ipInt ushr 16) and 0xFF}.${(ipInt ushr 8) and 0xFF}.${ipInt and 0xFF}"
    }

    fun calculateSubnetHosts(localIp: String, prefixLength: Int = 24): List<String> {
        val hosts = mutableListOf<String>()
        val ipInt = ipToInt(localIp)
        if (ipInt == 0) return listOf("127.0.0.1")

        // Standardize prefix to safe bounds for mobile scan (min /24 to prevent memory saturation)
        val safePrefix = prefixLength.coerceIn(24, 30)
        val mask = (-1 shl (32 - safePrefix))
        val network = ipInt and mask
        val broadcast = network or mask.inv()

        for (hostInt in (network + 1) until broadcast) {
            hosts.add(intToIp(hostInt))
        }
        return hosts
    }

    fun isPrivateOrLocal(ip: String): Boolean {
        if (ip == "127.0.0.1" || ip == "localhost" || ip.startsWith("10.0.2.")) return true
        val parts = ip.split(".").mapNotNull { it.toIntOrNull() }
        if (parts.size != 4) return false
        return (parts[0] == 10) ||
                (parts[0] == 172 && parts[1] in 16..31) ||
                (parts[0] == 192 && parts[1] == 168)
    }

    fun getAllNetworkInterfaces(): List<Map<String, String>> {
        val results = mutableListOf<Map<String, String>>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return emptyList()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses.toList().map { it.hostAddress }
                if (addrs.isNotEmpty()) {
                    results.add(
                        mapOf(
                            "name" to intf.name,
                            "displayName" to intf.displayName,
                            "isUp" to intf.isUp.toString(),
                            "isLoopback" to intf.isLoopback.toString(),
                            "isVirtual" to intf.isVirtual.toString(),
                            "addresses" to addrs.joinToString(", ")
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return results
    }
}
