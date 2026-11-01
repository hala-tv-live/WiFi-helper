package com.example.core

import com.example.data.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.coroutineContext

class DeviceDiscovery {

    private val commonProbePorts = intArrayOf(80, 443, 53, 22, 139, 445, 8080)

    suspend fun discoverDevices(
        localIp: String,
        gatewayIp: String,
        isLabMode: Boolean,
        onProgress: (scanned: Int, total: Int, currentDevice: DiscoveredDevice?) -> Unit
    ): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
        val discoveredList = mutableListOf<DiscoveredDevice>()
        val arpTable = NetworkUtils.getArpTable()

        val targetIps = if (isLabMode) {
            listOf("127.0.0.1", "10.0.2.2", "10.0.2.15")
        } else {
            NetworkUtils.calculateSubnetHosts(localIp, 24)
        }

        val total = targetIps.size
        var scanned = 0
        val semaphore = Semaphore(32) // Maximum 32 concurrent probes to prevent thread starvation

        // Always add self
        val selfDevice = DiscoveredDevice(
            ip = localIp,
            macAddress = arpTable[localIp] ?: "Device Host",
            hostname = "This Device (Local)",
            vendor = "Local Phone",
            isGateway = localIp == gatewayIp,
            isSelf = true,
            responseTimeMs = 1
        )
        discoveredList.add(selfDevice)
        onProgress(0, total, selfDevice)

        for (targetIp in targetIps) {
            if (!coroutineContext.isActive) break
            if (targetIp == localIp) {
                scanned++
                onProgress(scanned, total, null)
                continue
            }

            semaphore.withPermit {
                if (!coroutineContext.isActive) return@withPermit

                val device = probeHost(targetIp, gatewayIp, arpTable)
                scanned++
                if (device != null) {
                    synchronized(discoveredList) {
                        discoveredList.add(device)
                    }
                    onProgress(scanned, total, device)
                } else {
                    onProgress(scanned, total, null)
                }
            }
        }

        discoveredList.sortedWith(compareBy({ !it.isGateway }, { !it.isSelf }, { it.ip }))
    }

    private fun probeHost(
        ip: String,
        gatewayIp: String,
        arpTable: Map<String, String>
    ): DiscoveredDevice? {
        val startTime = System.currentTimeMillis()
        var reachable = false

        // Quick TCP connection probe across common ports
        for (port in commonProbePorts) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), 250)
                    reachable = true
                }
                if (reachable) break
            } catch (_: Exception) {}
        }

        // Fallback to InetAddress.isReachable
        if (!reachable) {
            try {
                val inet = InetAddress.getByName(ip)
                if (inet.isReachable(350)) {
                    reachable = true
                }
            } catch (_: Exception) {}
        }

        // If IP is present in ARP table with valid MAC, it is definitely active on LAN
        val mac = arpTable[ip]
        if (!reachable && !mac.isNullOrBlank()) {
            reachable = true
        }

        if (reachable || ip == gatewayIp) {
            val responseTime = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            var hostname = ip
            try {
                val inet = InetAddress.getByName(ip)
                val canonical = inet.canonicalHostName
                if (!canonical.isNullOrBlank() && canonical != ip) {
                    hostname = canonical
                }
            } catch (_: Exception) {}

            val resolvedMac = mac ?: "Unavailable"
            val vendor = if (ip == gatewayIp) {
                "Default Gateway / Router"
            } else {
                NetworkUtils.lookupVendor(mac)
            }

            return DiscoveredDevice(
                ip = ip,
                macAddress = resolvedMac,
                hostname = if (ip == gatewayIp && hostname == ip) "Router / Gateway" else hostname,
                vendor = vendor,
                isGateway = (ip == gatewayIp),
                isSelf = false,
                responseTimeMs = responseTime
            )
        }
        return null
    }
}
