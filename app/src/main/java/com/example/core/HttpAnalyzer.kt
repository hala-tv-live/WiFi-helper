package com.example.core

import com.example.data.HttpInspectionResult
import com.example.data.TlsCertInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class HttpAnalyzer {

    private val serviceDetector = ServiceDetector()

    private val securityHeaderNames = listOf(
        "Strict-Transport-Security",
        "Content-Security-Policy",
        "X-Frame-Options",
        "X-Content-Type-Options",
        "Referrer-Policy",
        "Permissions-Policy"
    )

    suspend fun analyze(rawUrl: String, timeoutMs: Int = 3000): HttpInspectionResult =
        withContext(Dispatchers.IO) {
            val normalized = when {
                rawUrl.startsWith("http://", ignoreCase = true) -> rawUrl
                rawUrl.startsWith("https://", ignoreCase = true) -> rawUrl
                else -> "http://$rawUrl"
            }

            val isHttps = normalized.startsWith("https://", ignoreCase = true)
            val startTime = System.currentTimeMillis()

            try {
                val urlObj = URL(normalized)
                val conn = urlObj.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = timeoutMs
                conn.readTimeout = timeoutMs
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("User-Agent", "NetworkSecurityLab/1.0 (Auditor)")

                conn.connect()
                val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                val responseCode = conn.responseCode
                val contentLength = conn.contentLengthLong.coerceAtLeast(0)

                val headers = mutableMapOf<String, String>()
                for ((key, value) in conn.headerFields) {
                    if (key != null && value.isNotEmpty()) {
                        headers[key] = value.joinToString(", ")
                    }
                }

                val foundSec = mutableListOf<String>()
                val missingSec = mutableListOf<String>()

                for (secHeader in securityHeaderNames) {
                    val matching = headers.keys.firstOrNull { it.equals(secHeader, ignoreCase = true) }
                    if (matching != null) {
                        foundSec.add(secHeader)
                    } else {
                        missingSec.add(secHeader)
                    }
                }

                // If HTTPS, inspect certificate
                var tlsCert: TlsCertInfo? = null
                if (isHttps) {
                    val host = urlObj.host
                    val port = if (urlObj.port > 0) urlObj.port else 443
                    tlsCert = serviceDetector.inspectTlsCertificate(host, port, timeoutMs)
                }

                conn.disconnect()

                HttpInspectionResult(
                    url = normalized,
                    responseCode = responseCode,
                    protocol = if (isHttps) "HTTPS (TLS)" else "HTTP (Plaintext)",
                    latencyMs = latency,
                    contentLength = contentLength,
                    isHttps = isHttps,
                    headers = headers,
                    securityHeadersFound = foundSec,
                    securityHeadersMissing = missingSec,
                    tlsCertInfo = tlsCert,
                    error = null
                )
            } catch (e: Exception) {
                val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                HttpInspectionResult(
                    url = normalized,
                    responseCode = 0,
                    protocol = if (isHttps) "HTTPS" else "HTTP",
                    latencyMs = latency,
                    isHttps = isHttps,
                    error = e.localizedMessage ?: "Connection failed"
                )
            }
        }
}
