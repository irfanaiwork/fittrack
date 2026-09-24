package com.example.updates

import java.text.DecimalFormat

data class AppUpdateInfo(
    val tagName: String,
    val versionName: String,
    val currentVersionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val publishedAt: String,
    val releasePageUrl: String,
    val apkDownloadUrl: String?,
    val apkFileName: String?,
    val apkSizeBytes: Long?,
    val isNewer: Boolean
) {
    val formattedApkSize: String?
        get() {
            if (apkSizeBytes == null || apkSizeBytes <= 0) return null
            val mb = apkSizeBytes / (1024.0 * 1024.0)
            return DecimalFormat("#,##0.1").format(mb) + " MB"
        }
}

sealed class UpdateCheckState {
    object Idle : UpdateCheckState()
    object Checking : UpdateCheckState()
    data class Available(val updateInfo: AppUpdateInfo) : UpdateCheckState()
    data class UpToDate(val currentVersion: String, val checkedAtMillis: Long = System.currentTimeMillis()) : UpdateCheckState()
    data class Error(val errorMessage: String) : UpdateCheckState()
}

object VersionComparator {
    /**
     * Returns true if [remote] version is strictly newer than [current].
     * Handles formats like "v1.1", "1.1.0", "v2.0-release", etc.
     */
    fun isNewerVersion(remote: String, current: String): Boolean {
        val cleanRemote = cleanVersion(remote)
        val cleanCurrent = cleanVersion(current)

        if (cleanRemote.isEmpty()) return false
        if (cleanCurrent.isEmpty()) return true
        if (cleanRemote == cleanCurrent) return false

        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    fun cleanVersion(v: String): String {
        return v.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .substringBefore("+")
            .trim()
    }
}
