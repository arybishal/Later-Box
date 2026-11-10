package com.laterbox.data.update

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GitHubReleaseService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
    private val repoOwner: String = "arybishal",
    private val repoName: String = "Later-Box"
) {

    private val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases"

    /**
     * Fetches the latest published non-draft, non-prerelease release.
     * Returns UpdateInfo if found, or null if no stable release with an APK is present.
     */
    fun fetchLatestRelease(
        currentVersionName: String,
        currentVersionCode: Long
    ): UpdateInfo? {
        val request = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "LaterBox-Android-App")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("GitHub API returned error code ${response.code}: ${response.message}")
            }

            val body = response.body?.string() ?: throw IOException("Empty response from GitHub API")
            return parseReleasesJson(body, currentVersionName, currentVersionCode)
        }
    }

    /**
     * Parses the GitHub releases JSON string. Extracted for easy unit testing without live network calls.
     */
    fun parseReleasesJson(
        jsonString: String,
        currentVersionName: String,
        currentVersionCode: Long
    ): UpdateInfo? {
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val releaseObj = jsonArray.getJSONObject(i)

            val isDraft = releaseObj.optBoolean("draft", false)
            val isPrerelease = releaseObj.optBoolean("prerelease", false)

            // Only examine stable published releases
            if (isDraft || isPrerelease) continue

            val tag = releaseObj.optString("tag_name", "").trim()
            val releaseName = releaseObj.optString("name", tag)
            val notes = releaseObj.optString("body", "")
            val publishedAt = releaseObj.optString("published_at", "")
            val releaseUrl = releaseObj.optString("html_url", "https://github.com/$repoOwner/$repoName/releases/tag/$tag")

            val assetsArray = releaseObj.optJSONArray("assets") ?: continue
            val apkAsset = findBestApkAsset(assetsArray) ?: continue

            val apkUrl = apkAsset.getString("browser_download_url")
            val apkName = apkAsset.getString("name")
            val apkSize = apkAsset.optLong("size", 0L)

            // Semantic version check
            val isNewer = SemanticVersion.isVersionNewer(tag, currentVersionName)

            val cleanVersionName = tag.removePrefix("v").removePrefix("V")
            // Attempt to derive or approximate a versionCode if semantic version is newer
            val targetVersionCode = if (isNewer) currentVersionCode + 1 else currentVersionCode

            return UpdateInfo(
                tag = tag,
                versionName = cleanVersionName,
                versionCode = targetVersionCode,
                name = releaseName,
                notes = notes,
                publishedAt = publishedAt,
                releaseUrl = releaseUrl,
                apkUrl = apkUrl,
                apkFileName = apkName,
                apkSizeBytes = apkSize,
                isNewer = isNewer
            )
        }

        return null
    }

    /**
     * Selects the best APK from assets:
     * 1. Must end with .apk
     * 2. Prefers assets containing "release" over "debug"
     * 3. Avoids "debug" APKs if a non-debug APK exists
     */
    private fun findBestApkAsset(assets: JSONArray): JSONObject? {
        val apkAssets = mutableListOf<JSONObject>()

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name", "").lowercase()
            if (name.endsWith(".apk")) {
                apkAssets.add(asset)
            }
        }

        if (apkAssets.isEmpty()) return null

        // Priority 1: release APK (e.g. laterbox-v1.3.1-release.apk)
        val releaseApk = apkAssets.find {
            val name = it.optString("name", "").lowercase()
            name.contains("release") && !name.contains("debug")
        }
        if (releaseApk != null) return releaseApk

        // Priority 2: non-debug APK
        val nonDebugApk = apkAssets.find {
            !it.optString("name", "").lowercase().contains("debug")
        }
        if (nonDebugApk != null) return nonDebugApk

        // Fallback: any APK
        return apkAssets.firstOrNull()
    }
}
