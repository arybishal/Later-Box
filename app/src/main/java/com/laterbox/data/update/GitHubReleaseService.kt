package com.laterbox.data.update

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Structured update-checking exceptions providing accurate, user-friendly error messages
 * without leaking misleading HTTP 404s when other issues occur.
 */
sealed class UpdateCheckException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class Offline(message: String = "You're offline. Connect to the internet and try again.") : UpdateCheckException(message)
    class RateLimited(message: String = "GitHub temporarily limited update requests. Please try again later.") : UpdateCheckException(message)
    class RepoNotFound(message: String = "The configured GitHub repository could not be found. Please check the update configuration.") : UpdateCheckException(message)
    class NoStableRelease(message: String = "No published stable release is currently available.") : UpdateCheckException(message)
    class NoCompatibleApk(message: String = "The latest release has no compatible APK asset.") : UpdateCheckException(message)
    class InvalidData(message: String = "GitHub returned an invalid release response.", cause: Throwable? = null) : UpdateCheckException(message, cause)
    class ServerUnavailable(message: String = "GitHub is temporarily unavailable. Please try again.") : UpdateCheckException(message)
    class Timeout(message: String = "Request timed out. Please check your internet connection and try again.") : UpdateCheckException(message)
    class Generic(message: String, cause: Throwable? = null) : UpdateCheckException(message, cause)
}

class GitHubReleaseService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
    val repoOwner: String = "arybishal",
    val repoName: String = "Later-Box"
) {

    private val latestReleaseUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
    private val releasesListUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases?per_page=20"

    private val sha256Pattern = Pattern.compile("(?i)(?:sha-?256|hash|checksum)?[\\s\\S]{0,100}?([a-f0-9]{64})")

    /**
     * Fetches the latest published non-draft, non-prerelease release.
     * Tries /releases/latest first; falls back to /releases list if latest returns 404.
     * Returns UpdateInfo if found, or null if no newer release exists.
     */
    fun fetchLatestRelease(
        currentVersionName: String,
        currentVersionCode: Long
    ): UpdateInfo? {
        // Step 1: Attempt to retrieve the designated latest release
        val latestResult = tryFetchEndpoint(latestReleaseUrl)

        when {
            latestResult is EndpointResult.Success -> {
                try {
                    val info = parseSingleReleaseJson(latestResult.body, currentVersionName, currentVersionCode)
                    if (info != null) return info
                    // If latest release was draft/prerelease or lacked APK, fall through to query list
                } catch (e: JSONException) {
                    throw UpdateCheckException.InvalidData("Invalid JSON from latest release endpoint", e)
                }
            }
            latestResult is EndpointResult.HttpError && latestResult.code == 404 -> {
                // /releases/latest returns 404 when no release is tagged as "latest"
                // Proceed to fallback releases list
            }
            latestResult is EndpointResult.HttpError -> {
                mapAndThrowHttpError(latestResult.code, latestResult.headers, latestResult.message)
            }
            latestResult is EndpointResult.NetworkError -> {
                mapAndThrowNetworkError(latestResult.throwable)
            }
        }

        // Step 2: Fallback to published releases list
        val listResult = tryFetchEndpoint(releasesListUrl)

        return when (listResult) {
            is EndpointResult.Success -> {
                try {
                    parseReleasesJson(listResult.body, currentVersionName, currentVersionCode)
                } catch (e: JSONException) {
                    throw UpdateCheckException.InvalidData("Invalid JSON array from releases list endpoint", e)
                }
            }
            is EndpointResult.HttpError -> {
                mapAndThrowHttpError(listResult.code, listResult.headers, listResult.message)
                null
            }
            is EndpointResult.NetworkError -> {
                mapAndThrowNetworkError(listResult.throwable)
                null
            }
        }
    }

    private sealed class EndpointResult {
        data class Success(val body: String) : EndpointResult()
        data class HttpError(val code: Int, val message: String, val headers: Map<String, String>) : EndpointResult()
        data class NetworkError(val throwable: Throwable) : EndpointResult()
    }

    private fun tryFetchEndpoint(url: String): EndpointResult {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "LaterBox-Android-App")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val headersMap = mutableMapOf<String, String>()
                for (name in response.headers.names()) {
                    headersMap[name.lowercase()] = response.header(name) ?: ""
                }

                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    EndpointResult.Success(body)
                } else {
                    EndpointResult.HttpError(response.code, response.message, headersMap)
                }
            }
        } catch (e: Throwable) {
            EndpointResult.NetworkError(e)
        }
    }

    private fun mapAndThrowHttpError(code: Int, headers: Map<String, String>, message: String): Nothing {
        when (code) {
            404 -> throw UpdateCheckException.RepoNotFound()
            403 -> {
                val remaining = headers["x-ratelimit-remaining"]?.toIntOrNull()
                if (remaining == 0) {
                    throw UpdateCheckException.RateLimited()
                } else {
                    throw UpdateCheckException.RateLimited("GitHub access was restricted (HTTP 403). Please try again later.")
                }
            }
            in 500..599 -> throw UpdateCheckException.ServerUnavailable()
            else -> throw UpdateCheckException.Generic("GitHub API returned error code $code: $message")
        }
    }

    private fun mapAndThrowNetworkError(e: Throwable): Nothing {
        when (e) {
            is UnknownHostException, is ConnectException -> throw UpdateCheckException.Offline()
            is SocketTimeoutException -> throw UpdateCheckException.Timeout()
            is UpdateCheckException -> throw e
            else -> throw UpdateCheckException.Generic("Network error: ${e.localizedMessage ?: "Unknown error"}", e)
        }
    }

    /**
     * Parses a single release JSON object (from /releases/latest).
     */
    fun parseSingleReleaseJson(
        jsonString: String,
        currentVersionName: String,
        currentVersionCode: Long
    ): UpdateInfo? {
        val obj = JSONObject(jsonString)
        return parseReleaseObject(obj, currentVersionName, currentVersionCode)
    }

    /**
     * Parses the GitHub releases list JSON string. Extracted for easy unit testing.
     */
    fun parseReleasesJson(
        jsonString: String,
        currentVersionName: String,
        currentVersionCode: Long
    ): UpdateInfo? {
        val jsonArray = JSONArray(jsonString)
        if (jsonArray.length() == 0) {
            throw UpdateCheckException.NoStableRelease()
        }

        var candidateWithoutApk: String? = null

        for (i in 0 until jsonArray.length()) {
            val releaseObj = jsonArray.getJSONObject(i)
            val isDraft = releaseObj.optBoolean("draft", false)
            val isPrerelease = releaseObj.optBoolean("prerelease", false)
            if (isDraft || isPrerelease) continue

            val info = parseReleaseObject(releaseObj, currentVersionName, currentVersionCode)
            if (info != null) {
                return info
            } else {
                if (candidateWithoutApk == null) {
                    candidateWithoutApk = releaseObj.optString("tag_name", "")
                }
            }
        }

        if (candidateWithoutApk != null) {
            throw UpdateCheckException.NoCompatibleApk()
        }

        throw UpdateCheckException.NoStableRelease()
    }

    private fun parseReleaseObject(
        releaseObj: JSONObject,
        currentVersionName: String,
        currentVersionCode: Long
    ): UpdateInfo? {
        val isDraft = releaseObj.optBoolean("draft", false)
        val isPrerelease = releaseObj.optBoolean("prerelease", false)
        if (isDraft || isPrerelease) return null

        val tag = releaseObj.optString("tag_name", "").trim()
        if (tag.isEmpty()) return null

        val releaseName = releaseObj.optString("name", tag)
        val notes = releaseObj.optString("body", "")
        val publishedAt = releaseObj.optString("published_at", "")
        val releaseUrl = releaseObj.optString("html_url", "https://github.com/$repoOwner/$repoName/releases/tag/$tag")

        val assetsArray = releaseObj.optJSONArray("assets") ?: return null
        val apkAsset = findBestApkAsset(assetsArray) ?: return null

        val apkUrl = apkAsset.getString("browser_download_url")
        val apkName = apkAsset.getString("name")
        val apkSize = apkAsset.optLong("size", 0L)

        // Extract SHA-256 checksum from asset digest (sha256:...) or release notes
        val expectedSha256 = extractSha256(apkAsset, notes)

        val isNewer = SemanticVersion.isVersionNewer(tag, currentVersionName)
        val cleanVersionName = tag.removePrefix("v").removePrefix("V")
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
            isNewer = isNewer,
            expectedSha256 = expectedSha256
        )
    }

    private fun extractSha256(asset: JSONObject, releaseNotes: String): String? {
        // 1. Check digest field on asset (GitHub CLI uploads often add sha256:...)
        val digest = asset.optString("digest", "")
        if (digest.startsWith("sha256:", ignoreCase = true)) {
            val hash = digest.substringAfter("sha256:").trim()
            if (hash.length == 64) return hash
        }

        // 2. Search release notes for 64-char hex string
        val matcher = sha256Pattern.matcher(releaseNotes)
        if (matcher.find()) {
            val candidate = matcher.group(1)
            if (candidate.length == 64) return candidate
        }

        return null
    }

    /**
     * Selects the best APK from assets:
     * 1. Must end with .apk
     * 2. Must NOT contain "debug" (ignores debug builds)
     * 3. Prefers assets containing "release"
     * 4. Validates that browser_download_url is present and starts with http(s)
     */
    fun findBestApkAsset(assets: JSONArray): JSONObject? {
        val validApks = mutableListOf<JSONObject>()

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name", "").lowercase()
            val url = asset.optString("browser_download_url", "")

            // Ignore non-APK files and debug APKs
            if (!name.endsWith(".apk") || name.contains("debug")) {
                continue
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                validApks.add(asset)
            }
        }

        if (validApks.isEmpty()) return null

        // Priority 1: release APK (e.g. app-release.apk, laterbox-release.apk)
        val releaseApk = validApks.find {
            it.optString("name", "").lowercase().contains("release")
        }
        if (releaseApk != null) return releaseApk

        // Priority 2: first valid non-debug APK
        return validApks.firstOrNull()
    }
}
