package com.example.core

import com.example.data.PortScanItem
import com.example.data.PortState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.coroutines.coroutineContext

class PortScanner {

    companion object {
        val COMMON_PORTS = listOf(
            21, 22, 23, 25, 53, 80, 110, 139, 143, 443, 445, 554, 8080, 8443
        )

        val EXTENDED_SECURITY_PORTS = listOf(
            21, 22, 23, 25, 53, 80, 110, 135, 139, 143, 161, 443, 445, 554,
            631, 993, 995, 1433, 1521, 1883, 3306, 3389, 5000, 5432, 5900, 6379, 8000, 8080, 8443, 8888, 9000, 27017
        )
    }

    suspend fun scanPorts(
        targetIp: String,
        portsToScan: List<Int>,
        timeoutMs: Int = 1000,
        onProgress: (scanned: Int, total: Int, item: PortScanItem) -> Unit
    ): List<PortScanItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PortScanItem>()
        val total = portsToScan.size
        var scanned = 0
        val semaphore = Semaphore(50) // Thread pool bounded to 50 concurrent sockets

        for (port in portsToScan) {
            if (!coroutineContext.isActive) break

            semaphore.withPermit {
                if (!coroutineContext.isActive) return@withPermit

                val item = testPort(targetIp, port, timeoutMs)
                scanned++
                synchronized(results) {
                    results.add(item)
                }
                onProgress(scanned, total, item)
            }
        }

        results.sortedWith(compareBy({ it.state != PortState.OPEN }, { it.port }))
    }

    private fun testPort(ip: String, port: Int, timeoutMs: Int): PortScanItem {
        val serviceName = NetworkUtils.getServiceName(port)
        val startTime = System.currentTimeMillis()

        return try {
            Socket().use { socket ->
                socket.soTimeout = timeoutMs
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(1)

                PortScanItem(
                    port = port,
                    serviceName = serviceName,
                    state = PortState.OPEN,
                    responseTimeMs = duration
                )
            }
        } catch (e: ConnectException) {
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            PortScanItem(
                port = port,
                serviceName = serviceName,
                state = PortState.CLOSED,
                responseTimeMs = duration
            )
        } catch (e: SocketTimeoutException) {
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            PortScanItem(
                port = port,
                serviceName = serviceName,
                state = PortState.FILTERED,
                responseTimeMs = duration
            )
        } catch (_: Exception) {
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            PortScanItem(
                port = port,
                serviceName = serviceName,
                state = PortState.FILTERED,
                responseTimeMs = duration
            )
        }
    }
}
