package com.example.core

import com.example.data.PingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class PingTester {

    suspend fun ping(targetHost: String, count: Int = 4): PingResult =
        withContext(Dispatchers.IO) {
            val host = targetHost.trim()
                .removePrefix("http://")
                .removePrefix("https://")
                .substringBefore("/")

            val logBuilder = StringBuilder()
            var binarySucceeded = false
            var minRtt = 0f
            var avgRtt = 0f
            var maxRtt = 0f
            var received = 0
            var packetLoss = 100

            try {
                val process = ProcessBuilder(
                    "/system/bin/ping",
                    "-c", count.toString(),
                    "-W", "2",
                    host
                ).redirectErrorStream(true).start()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    logBuilder.appendLine(line)
                    line?.let { l ->
                        if (l.contains("packets transmitted")) {
                            // Example: "4 packets transmitted, 4 received, 0% packet loss"
                            val parts = l.split(",")
                            for (p in parts) {
                                if (p.contains("received")) {
                                    received = p.trim().substringBefore(" ").toIntOrNull() ?: 0
                                } else if (p.contains("packet loss")) {
                                    packetLoss = p.trim().substringBefore("%").substringAfterLast(" ").toIntOrNull() ?: 100
                                }
                            }
                        } else if (l.contains("rtt min/avg/max") || l.contains("round-trip min/avg/max")) {
                            // Example: "rtt min/avg/max/mdev = 12.345/14.567/18.910/2.123 ms"
                            val stats = l.substringAfter("=").trim().substringBefore(" ms").split("/")
                            if (stats.size >= 3) {
                                minRtt = stats[0].toFloatOrNull() ?: 0f
                                avgRtt = stats[1].toFloatOrNull() ?: 0f
                                maxRtt = stats[2].toFloatOrNull() ?: 0f
                            }
                        }
                    }
                }
                process.waitFor()
                if (logBuilder.isNotEmpty() && received > 0) {
                    binarySucceeded = true
                }
            } catch (e: Exception) {
                logBuilder.appendLine("Ping binary unavailable: ${e.message}. Using socket fallback...")
            }

            // Fallback if binary failed or restricted
            if (!binarySucceeded) {
                var successfulPackets = 0
                val rttList = mutableListOf<Float>()

                for (i in 1..count) {
                    val start = System.currentTimeMillis()
                    var success = false
                    try {
                        val inet = InetAddress.getByName(host)
                        if (inet.isReachable(1500)) {
                            success = true
                        } else {
                            // Try quick TCP probe to port 80 or 443
                            Socket().use { s ->
                                s.connect(InetSocketAddress(host, 80), 1000)
                                success = true
                            }
                        }
                    } catch (_: Exception) {}

                    val duration = (System.currentTimeMillis() - start).toFloat()
                    if (success) {
                        successfulPackets++
                        rttList.add(duration)
                        logBuilder.appendLine("Reply from $host: seq=$i time=${duration.toInt()}ms")
                    } else {
                        logBuilder.appendLine("Request timed out for seq=$i")
                    }
                }

                received = successfulPackets
                packetLoss = (((count - received).toFloat() / count) * 100).toInt()
                if (rttList.isNotEmpty()) {
                    minRtt = rttList.minOrNull() ?: 0f
                    maxRtt = rttList.maxOrNull() ?: 0f
                    avgRtt = rttList.average().toFloat()
                }
            }

            PingResult(
                host = host,
                packetsSent = count,
                packetsReceived = received,
                packetLossPercent = packetLoss,
                minRttMs = minRtt,
                avgRttMs = avgRtt,
                maxRttMs = maxRtt,
                outputLog = logBuilder.toString().trim(),
                isSuccess = received > 0
            )
        }
}
