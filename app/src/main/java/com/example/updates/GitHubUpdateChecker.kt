package com.example.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GitHubUpdateChecker {
    private const val TAG = "GitHubUpdateChecker"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Queries the latest release of the given GitHub repo.
     */
    suspend fun checkLatestRelease(
        owner: String,
        repo: String,
        currentVersionName: String = BuildConfig.VERSION_NAME
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val cleanOwner = owner.trim().removePrefix("@")
        val cleanRepo = repo.trim()

        if (cleanOwner.isEmpty() || cleanRepo.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("GitHub username/repo cannot be empty. Please enter owner and repository name.")
            )
        }

        val apiUrl = "https://api.github.com/repos/$cleanOwner/$cleanRepo/releases/latest"
        Log.d(TAG, "Checking update at: $apiUrl")

        try {
            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "FitTrack-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext when (code) {
                    404 -> Result.failure(
                        Exception("No release found for '$cleanOwner/$cleanRepo'. Check if repo is public and a release is published.")
                    )
                    403 -> Result.failure(
                        Exception("GitHub API rate limit reached. Please try again in a few minutes.")
                    )
                    else -> Result.failure(
                        Exception("GitHub error (Code $code): ${response.message}")
                    )
                }
            }

            if (bodyString.isEmpty()) {
                return@withContext Result.failure(Exception("Empty response from GitHub."))
            }

            val json = JSONObject(bodyString)
            val tagName = json.optString("tag_name", "").trim()
            val releaseTitle = json.optString("name", tagName)
            val releaseNotes = json.optString("body", "No release notes provided.")
            val htmlUrl = json.optString("html_url", "https://github.com/$cleanOwner/$cleanRepo/releases")
            val publishedAt = json.optString("published_at", "")

            // Look for an APK asset in the release
            var apkDownloadUrl: String? = null
            var apkFileName: String? = null
            var apkSizeBytes: Long? = null

            val assetsArray = json.optJSONArray("assets")
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkFileName = name
                        apkDownloadUrl = asset.optString("browser_download_url")
                        apkSizeBytes = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            val cleanRemoteVersion = VersionComparator.cleanVersion(tagName)
            val cleanLocalVersion = VersionComparator.cleanVersion(currentVersionName)
            val isNewer = VersionComparator.isNewerVersion(tagName, currentVersionName)

            val info = AppUpdateInfo(
                tagName = tagName,
                versionName = cleanRemoteVersion.ifEmpty { tagName },
                currentVersionName = cleanLocalVersion.ifEmpty { currentVersionName },
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                publishedAt = publishedAt,
                releasePageUrl = htmlUrl,
                apkDownloadUrl = apkDownloadUrl,
                apkFileName = apkFileName,
                apkSizeBytes = apkSizeBytes,
                isNewer = isNewer
            )

            Log.d(TAG, "Parsed release: tag=$tagName, isNewer=$isNewer, apk=$apkDownloadUrl")
            Result.success(info)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check update", e)
            Result.failure(e)
        }
    }

    /**
     * Opens the direct APK download URL or GitHub release page in the browser
     */
    fun openUpdateLink(context: Context, updateInfo: AppUpdateInfo) {
        val targetUrl = updateInfo.apkDownloadUrl ?: updateInfo.releasePageUrl
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open update URL: $targetUrl", e)
        }
    }

    /**
     * Opens the GitHub release page for full details and changelog
     */
    fun openReleasePage(context: Context, updateInfo: AppUpdateInfo) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.releasePageUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open release page", e)
        }
    }

    /**
     * Generates a sample update object for testing & previewing the dialog
     */
    fun createMockUpdateInfo(currentVersion: String = BuildConfig.VERSION_NAME): AppUpdateInfo {
        return AppUpdateInfo(
            tagName = "v1.1.0",
            versionName = "1.1.0",
            currentVersionName = currentVersion,
            releaseTitle = "FitTrack v1.1.0 — New Features & Improvements",
            releaseNotes = """
                🚀 What's New in this update:
                • In-App GitHub Updates: Seamless automatic update notifications
                • Enhanced Calorie & BMR Calculator: Accurate Mifflin-St Jeor metabolic tracking
                • Non-Stop Incoming Call Alarm: Never miss your diet or workout reminders
                • Water Intake Counter: Quick logging with custom daily hydration goals
                • Bug fixes and smooth performance improvements
            """.trimIndent(),
            publishedAt = "Just now",
            releasePageUrl = "https://github.com/irfan-aiwork/fit-track/releases/latest",
            apkDownloadUrl = "https://github.com/irfan-aiwork/fit-track/releases/latest",
            apkFileName = "FitTrack-v1.1.0.apk",
            apkSizeBytes = 18_400_000L,
            isNewer = true
        )
    }
}
