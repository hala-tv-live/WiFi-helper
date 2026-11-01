package com.example.core

import com.example.data.DnsInspectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class DnsTester {

    suspend fun resolve(domain: String, activeDnsList: List<String>): DnsInspectionResult =
        withContext(Dispatchers.IO) {
            val cleanDomain = domain.trim()
                .removePrefix("http://")
                .removePrefix("https://")
                .substringBefore("/")
                .substringBefore(":")

            val startTime = System.currentTimeMillis()
            try {
                val addresses = InetAddress.getAllByName(cleanDomain)
                val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1)

                val ipList = addresses.map { it.hostAddress ?: "" }.filter { it.isNotBlank() }
                val canonical = addresses.firstOrNull()?.canonicalHostName ?: cleanDomain

                DnsInspectionResult(
                    domain = cleanDomain,
                    addresses = ipList,
                    latencyMs = latency,
                    canonicalName = canonical,
                    activeDnsServers = activeDnsList,
                    error = null
                )
            } catch (e: Exception) {
                val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                DnsInspectionResult(
                    domain = cleanDomain,
                    addresses = emptyList(),
                    latencyMs = latency,
                    activeDnsServers = activeDnsList,
                    error = e.localizedMessage ?: "DNS resolution failed"
                )
            }
        }
}
