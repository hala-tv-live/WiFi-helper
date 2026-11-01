package com.example.data

import android.content.Context
import android.content.SharedPreferences

class LocalStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nslab_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DISCLAIMER_ACCEPTED = "key_disclaimer_accepted"
        private const val KEY_IS_ARABIC = "key_is_arabic"
        private const val KEY_LOCAL_LAB_MODE = "key_local_lab_mode"
        private const val KEY_TIMEOUT_MS = "key_timeout_ms"
        private const val KEY_IS_DARK_MODE = "key_is_dark_mode"
        private const val KEY_WATCHDOG_ENABLED = "key_watchdog_enabled"
        private const val KEY_WATCHDOG_INTERVAL = "key_watchdog_interval"
        private const val KEY_KNOWN_MACS = "key_known_macs"
        private const val KEY_WATCHDOG_ALERTS = "key_watchdog_alerts"
    }

    fun isDisclaimerAccepted(): Boolean {
        return prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
    }

    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    fun isArabic(): Boolean {
        return prefs.getBoolean(KEY_IS_ARABIC, true) // Arabic default per requirements
    }

    fun setArabic(isArabic: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ARABIC, isArabic).apply()
    }

    fun isLocalLabMode(): Boolean {
        return prefs.getBoolean(KEY_LOCAL_LAB_MODE, false)
    }

    fun setLocalLabMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCAL_LAB_MODE, enabled).apply()
    }

    fun getTimeoutMs(): Int {
        return prefs.getInt(KEY_TIMEOUT_MS, 1000)
    }

    fun setTimeoutMs(timeoutMs: Int) {
        prefs.edit().putInt(KEY_TIMEOUT_MS, timeoutMs).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_IS_DARK_MODE, true)
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
    }

    fun isWatchdogEnabled(): Boolean {
        return prefs.getBoolean(KEY_WATCHDOG_ENABLED, false)
    }

    fun setWatchdogEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WATCHDOG_ENABLED, enabled).apply()
    }

    fun getWatchdogIntervalMinutes(): Int {
        return prefs.getInt(KEY_WATCHDOG_INTERVAL, 15)
    }

    fun setWatchdogIntervalMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_WATCHDOG_INTERVAL, minutes).apply()
    }

    fun getKnownDeviceMacs(): Set<String> {
        return prefs.getStringSet(KEY_KNOWN_MACS, emptySet()) ?: emptySet()
    }

    fun setKnownDeviceMacs(macs: Set<String>) {
        prefs.edit().putStringSet(KEY_KNOWN_MACS, macs).apply()
    }

    fun addKnownDeviceMac(mac: String) {
        val current = getKnownDeviceMacs().toMutableSet()
        current.add(mac.uppercase())
        setKnownDeviceMacs(current)
    }

    fun getWatchdogAlerts(): List<WatchdogAlert> {
        val raw = prefs.getString(KEY_WATCHDOG_ALERTS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 5) {
                WatchdogAlert(
                    id = parts[0],
                    timestamp = parts[1].toLongOrNull() ?: 0L,
                    ip = parts[2],
                    mac = parts[3],
                    vendor = parts[4]
                )
            } else null
        }
    }

    fun addWatchdogAlert(alert: WatchdogAlert) {
        val current = getWatchdogAlerts().toMutableList()
        current.add(0, alert)
        val serialized = current.take(30).joinToString(";;") {
            "${it.id}|${it.timestamp}|${it.ip}|${it.mac}|${it.vendor}"
        }
        prefs.edit().putString(KEY_WATCHDOG_ALERTS, serialized).apply()
    }

    fun getPreferences(): AppPreferences {
        return AppPreferences(
            disclaimerAccepted = isDisclaimerAccepted(),
            isArabic = isArabic(),
            localLabMode = isLocalLabMode(),
            timeoutMs = getTimeoutMs(),
            isDarkMode = isDarkMode(),
            watchdogEnabled = isWatchdogEnabled(),
            watchdogIntervalMinutes = getWatchdogIntervalMinutes(),
            knownDeviceMacs = getKnownDeviceMacs(),
            watchdogAlerts = getWatchdogAlerts()
        )
    }
}
