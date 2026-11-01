package com.example.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.LocalStorage
import com.example.data.WatchdogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class NetworkWatchdogJobService : JobService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartJob(params: JobParameters?): Boolean {
        serviceScope.launch {
            try {
                executeSubnetAudit()
            } finally {
                jobFinished(params, false)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        serviceJob.cancel()
        return true
    }

    private suspend fun executeSubnetAudit() {
        val localStorage = LocalStorage(applicationContext)
        if (!localStorage.isWatchdogEnabled()) return

        val networkInfoProvider = NetworkInfoProvider(applicationContext)
        val netInfo = networkInfoProvider.getNetworkInfo()
        if (netInfo.localIp == "0.0.0.0" || netInfo.localIp == "127.0.0.1") return

        val deviceDiscovery = DeviceDiscovery()
        val discovered = deviceDiscovery.discoverDevices(
            localIp = netInfo.localIp,
            gatewayIp = netInfo.gatewayIp,
            isLabMode = localStorage.isLocalLabMode(),
            onProgress = { _, _, _ -> }
        )

        val knownMacs = localStorage.getKnownDeviceMacs()
        val isArabic = localStorage.isArabic()

        // If known list is empty, initialize with current devices
        if (knownMacs.isEmpty()) {
            val initialSet = discovered.map { it.macAddress.uppercase() }.filter { it != "UNKNOWN" && it != "DEVICE HOST" }.toSet()
            localStorage.setKnownDeviceMacs(initialSet)
            return
        }

        // Check for rogue or unfamiliar devices
        for (device in discovered) {
            val cleanMac = device.macAddress.uppercase()
            if (cleanMac.isNotBlank() && cleanMac != "UNKNOWN" && cleanMac != "DEVICE HOST") {
                if (!knownMacs.contains(cleanMac)) {
                    // New Device Found!
                    val alert = WatchdogAlert(
                        id = UUID.randomUUID().toString(),
                        timestamp = System.currentTimeMillis(),
                        ip = device.ip,
                        mac = cleanMac,
                        vendor = device.vendor
                    )
                    localStorage.addWatchdogAlert(alert)
                    sendNotification(alert, isArabic)
                }
            }
        }
    }

    private fun sendNotification(alert: WatchdogAlert, isArabic: Boolean) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "nslab_watchdog_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                if (isArabic) "حارس الشبكة (Network Watchdog)" else "Network Watchdog Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when unauthorized or new devices join your local network"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isArabic) "⚠️ جهاز جديد متصل بالشبكة!" else "⚠️ New Device Detected on Network!"
        val content = if (isArabic) {
            "العنوان: ${alert.ip}  •  المصنع: ${alert.vendor}  •  MAC: ${alert.mac}"
        } else {
            "IP: ${alert.ip}  •  Vendor: ${alert.vendor}  •  MAC: ${alert.mac}"
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alert.id.hashCode(), notification)
    }

    companion object {
        private const val WATCHDOG_JOB_ID = 9021

        fun scheduleWatchdog(context: Context, intervalMinutes: Int) {
            val jobScheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val componentName = ComponentName(context, NetworkWatchdogJobService::class.java)

            // Minimum periodic interval on Android is 15 minutes
            val intervalMs = (intervalMinutes.coerceAtLeast(15) * 60 * 1000).toLong()

            val jobInfo = JobInfo.Builder(WATCHDOG_JOB_ID, componentName)
                .setPeriodic(intervalMs)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // Wi-Fi only to save mobile data
                .setPersisted(false)
                .build()

            jobScheduler.schedule(jobInfo)
        }

        fun cancelWatchdog(context: Context) {
            val jobScheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(WATCHDOG_JOB_ID)
        }
    }
}
