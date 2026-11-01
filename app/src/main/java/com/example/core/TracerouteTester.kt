package com.example.core

import com.example.data.TracerouteHop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.coroutineContext

class TracerouteTester {

    suspend fun trace(
        targetHost: String,
        maxHops: Int = 15,
        onHopFound: (TracerouteHop) -> Unit
    ): List<TracerouteHop> = withContext(Dispatchers.IO) {
        val hops = mutableListOf<TracerouteHop>()
        val host = targetHost.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")

        for (ttl in 1..maxHops) {
            if (!coroutineContext.isActive) break

            val startTime = System.currentTimeMillis()
            var hopIp = "* * *"
            var isReached = false

            try {
                // Execute single ping with specific TTL
                val process = ProcessBuilder(
                    "/system/bin/ping",
                    "-c", "1",
                    "-t", ttl.toString(),
                    "-W", "1",
                    host
                ).redirectErrorStream(true).start()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let { l ->
                        if (l.contains("From ", ignoreCase = true)) {
                            // Example: "From 192.168.1.1: icmp_seq=1 Time to live exceeded"
                            val extracted = l.substringAfter("From ").substringBefore(":").substringBefore(" ")
                            if (extracted.isNotBlank()) hopIp = extracted
                        } else if (l.contains("bytes from ", ignoreCase = true)) {
                            // Destination reached!
                            val extracted = l.substringAfter("bytes from ").substringBefore(":").substringBefore(" ")
                            if (extracted.isNotBlank()) hopIp = extracted
                            isReached = true
                        }
                    }
                }
                process.waitFor()
            } catch (_: Exception) {}

            val rtt = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            val hop = TracerouteHop(
                hopNumber = ttl,
                ip = hopIp,
                rttMs = rtt,
                isReached = isReached
            )
            hops.add(hop)
            onHopFound(hop)

            if (isReached) {
                break
            }
        }
        hops
    }
}
