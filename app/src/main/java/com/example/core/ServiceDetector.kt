package com.example.core

import com.example.data.TlsCertInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ServiceDetector {

    suspend fun grabBanner(ip: String, port: Int, timeoutMs: Int = 1500): String? =
        withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.soTimeout = timeoutMs
                    socket.connect(InetSocketAddress(ip, port), timeoutMs)

                    // Some services wait for a newline or HTTP request to send a banner
                    val os = socket.getOutputStream()
                    if (port == 80 || port == 8080 || port == 8000) {
                        os.write("HEAD / HTTP/1.0\r\n\r\n".toByteArray())
                        os.flush()
                    }

                    val buffer = ByteArray(1024)
                    val read = socket.getInputStream().read(buffer)
                    if (read > 0) {
                        val banner = String(buffer, 0, read).trim()
                        if (banner.isNotBlank()) {
                            return@withContext banner.take(300)
                        }
                    }
                }
            } catch (_: Exception) {}
            null
        }

    suspend fun inspectTlsCertificate(ip: String, port: Int = 443, timeoutMs: Int = 2000): TlsCertInfo? =
        withContext(Dispatchers.IO) {
            try {
                var capturedCert: X509Certificate? = null

                // Custom TrustManager that extracts the server certificate without failing on self-signed
                val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
                        if (certs.isNotEmpty()) {
                            capturedCert = certs[0]
                        }
                    }
                })

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAll, java.security.SecureRandom())
                val factory = sslContext.socketFactory

                val socket = factory.createSocket() as SSLSocket
                socket.soTimeout = timeoutMs
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                socket.startHandshake()

                val cert = capturedCert ?: socket.session.peerCertificates.firstOrNull() as? X509Certificate
                socket.close()

                if (cert != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    val notAfter = cert.notAfter
                    val isExpired = notAfter.before(Date())
                    val isSelfSigned = cert.issuerDN.name == cert.subjectDN.name

                    return@withContext TlsCertInfo(
                        subject = cert.subjectDN.name,
                        issuer = cert.issuerDN.name,
                        validFrom = dateFormat.format(cert.notBefore),
                        validTo = dateFormat.format(notAfter),
                        serialNumber = cert.serialNumber.toString(16).uppercase(),
                        signatureAlgorithm = cert.sigAlgName,
                        isExpired = isExpired,
                        isSelfSigned = isSelfSigned
                    )
                }
            } catch (_: Exception) {}
            null
        }

    suspend fun inspectHttpEndpoint(targetUrl: String, timeoutMs: Int = 3000): Map<String, String> =
        withContext(Dispatchers.IO) {
            val headersMap = mutableMapOf<String, String>()
            try {
                val url = URL(if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) targetUrl else "http://$targetUrl")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "HEAD"
                conn.connectTimeout = timeoutMs
                conn.readTimeout = timeoutMs
                conn.instanceFollowRedirects = false
                conn.setRequestProperty("User-Agent", "NetworkSecurityLab/1.0 (Auditor)")

                conn.connect()
                headersMap["Status"] = "${conn.responseCode} ${conn.responseMessage}"
                for ((key, value) in conn.headerFields) {
                    if (key != null && value.isNotEmpty()) {
                        headersMap[key] = value.joinToString(", ")
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                headersMap["Error"] = e.message ?: "Failed to connect"
            }
            headersMap
        }
}
