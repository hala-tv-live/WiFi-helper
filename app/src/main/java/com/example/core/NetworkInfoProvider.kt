package com.example.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.example.data.ConnectionType
import com.example.data.NetworkInfoData
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

class NetworkInfoProvider(private val context: Context) {

    fun getNetworkInfo(): NetworkInfoData {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        var connType = ConnectionType.OFFLINE
        var localIp = "127.0.0.1"
        var gatewayIp = "Unknown"
        var subnetMask = "255.255.255.0"
        var networkCidr = "127.0.0.1/24"
        val dnsServers = mutableListOf<String>()
        var ssid = "Disconnected"
        var bssid = "00:00:00:00:00:00"
        var interfaceName = "lo"
        var linkSpeed = 0
        var frequency = 0

        connectivityManager?.let { cm ->
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            val linkProperties = cm.getLinkProperties(activeNetwork)

            if (capabilities != null) {
                connType = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                    else -> ConnectionType.OFFLINE
                }
            }

            linkProperties?.let { lp ->
                interfaceName = lp.interfaceName ?: "wlan0"

                // Extract DNS
                for (dns in lp.dnsServers) {
                    dns.hostAddress?.let { dnsServers.add(it) }
                }

                // Extract Gateway (Routes)
                for (route in lp.routes) {
                    if (route.isDefaultRoute && route.gateway != null) {
                        gatewayIp = route.gateway?.hostAddress ?: gatewayIp
                    }
                }

                // Extract Link Addresses
                for (la in lp.linkAddresses) {
                    val addr = la.address
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        localIp = addr.hostAddress ?: localIp
                        val prefix = la.prefixLength
                        networkCidr = "$localIp/$prefix"
                        subnetMask = prefixToSubnetMask(prefix)
                    }
                }
            }
        }

        // If local IP not resolved via LinkProperties, query NetworkInterface
        if (localIp == "127.0.0.1" || localIp == "0.0.0.0") {
            try {
                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    if (intf.isUp && !intf.isLoopback) {
                        for (addr in Collections.list(intf.inetAddresses)) {
                            if (addr is Inet4Address && !addr.isLoopbackAddress) {
                                localIp = addr.hostAddress ?: localIp
                                interfaceName = intf.name
                                break
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // Wi-Fi Details
        if (connType == ConnectionType.WIFI && wifiManager != null) {
            try {
                val wifiInfo: WifiInfo? = wifiManager.connectionInfo
                if (wifiInfo != null) {
                    val rawSsid = wifiInfo.ssid
                    ssid = if (!rawSsid.isNullOrBlank() && rawSsid != "<unknown ssid>") {
                        rawSsid.replace("\"", "")
                    } else {
                        "Wi-Fi Active"
                    }
                    bssid = wifiInfo.bssid ?: "00:00:00:00:00:00"
                    linkSpeed = wifiInfo.linkSpeed
                    frequency = wifiInfo.frequency
                }

                // Fallback for gateway via DhcpInfo if not found
                if (gatewayIp == "Unknown" || gatewayIp == "0.0.0.0") {
                    val dhcp = wifiManager.dhcpInfo
                    if (dhcp != null && dhcp.gateway != 0) {
                        gatewayIp = NetworkUtils.intToIp(dhcp.gateway)
                    }
                }
            } catch (_: Exception) {}
        }

        if (dnsServers.isEmpty()) {
            dnsServers.add("8.8.8.8")
            dnsServers.add("1.1.1.1")
        }

        return NetworkInfoData(
            localIp = localIp,
            gatewayIp = gatewayIp,
            subnetMask = subnetMask,
            networkCidr = networkCidr,
            dnsServers = dnsServers,
            connectionType = connType,
            ssid = ssid,
            bssid = bssid,
            interfaceName = interfaceName,
            macAddress = "Hidden by OS",
            linkSpeedMbps = linkSpeed,
            frequencyMhz = frequency
        )
    }

    private fun prefixToSubnetMask(prefix: Int): String {
        val shift = (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
        return "${(shift shr 24) and 0xFF}.${(shift shr 16) and 0xFF}.${(shift shr 8) and 0xFF}.${shift and 0xFF}"
    }
}
