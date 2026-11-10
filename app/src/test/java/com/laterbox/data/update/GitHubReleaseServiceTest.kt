package com.laterbox.data.update

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GitHubReleaseServiceTest {

    private val defaultService = GitHubReleaseService()

    private fun mockClient(responder: (Request) -> Response): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain -> responder(chain.request()) }
            .build()
    }

    private fun jsonResponse(
        request: Request,
        code: Int,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): Response {
        val builder = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(body.toResponseBody("application/json".toMediaType()))
        headers.forEach { (k, v) -> builder.addHeader(k, v) }
        return builder.build()
    }

    // =========================================================================
    // SCENARIO 1: Valid repo response parses properly
    // =========================================================================
    @Test
    fun testParseStableReleaseWithApkAsset() {
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "name": "Later Box v1.9.0 - Settings Theme Correction & Update Checker Repair",
                "body": "SHA256: 1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef\n- Fixed GitHub 404",
                "draft": false,
                "prerelease": false,
                "published_at": "2026-11-10T12:00:00Z",
                "html_url": "https://github.com/arybishal/Later-Box/releases/tag/v1.9.0",
                "assets": [
                    {
                        "name": "app-release.apk",
                        "size": 18000000,
                        "browser_download_url": "https://github.com/arybishal/Later-Box/releases/download/v1.9.0/app-release.apk"
                    }
                ]
            }
        ]
        """.trimIndent()

        val updateInfo = defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
        assertNotNull(updateInfo)
        assertEquals("v1.9.0", updateInfo!!.tag)
        assertEquals("1.9.0", updateInfo.versionName)
        assertTrue(updateInfo.isNewer)
        assertEquals("app-release.apk", updateInfo.apkFileName)
        assertEquals(18000000L, updateInfo.apkSizeBytes)
        assertEquals("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef", updateInfo.expectedSha256)
    }

    // =========================================================================
    // SCENARIO 2: Primary latest release lookup (/releases/latest) succeeds
    // =========================================================================
    @Test
    fun testFetchLatestRelease_PrimaryEndpointSuccess() {
        val singleReleaseJson = """
        {
            "tag_name": "v1.9.0",
            "name": "Later Box v1.9.0",
            "body": "Latest release notes",
            "draft": false,
            "prerelease": false,
            "assets": [
                {
                    "name": "app-release.apk",
                    "size": 16500000,
                    "browser_download_url": "https://github.com/arybishal/Later-Box/releases/download/v1.9.0/app-release.apk"
                }
            ]
        }
        """.trimIndent()

        val client = mockClient { req ->
            if (req.url.encodedPath.endsWith("/releases/latest")) {
                jsonResponse(req, 200, singleReleaseJson)
            } else {
                jsonResponse(req, 404, "Not Found")
            }
        }

        val service = GitHubReleaseService(client = client)
        val info = service.fetchLatestRelease("1.8.0", 13L)
        assertNotNull(info)
        assertEquals("v1.9.0", info!!.tag)
        assertTrue(info.isNewer)
    }

    // =========================================================================
    // SCENARIO 3: Fallback to /releases list succeeds when latest returns 404
    // =========================================================================
    @Test
    fun testFetchLatestRelease_FallbackToListWhenLatest404() {
        val releasesListJson = """
        [
            {
                "tag_name": "v1.9.0",
                "name": "Later Box v1.9.0",
                "body": "Fallback release notes",
                "draft": false,
                "prerelease": false,
                "assets": [
                    {
                        "name": "LaterBox-release.apk",
                        "size": 17000000,
                        "browser_download_url": "https://github.com/arybishal/Later-Box/releases/download/v1.9.0/LaterBox-release.apk"
                    }
                ]
            }
        ]
        """.trimIndent()

        val client = mockClient { req ->
            if (req.url.encodedPath.endsWith("/releases/latest")) {
                jsonResponse(req, 404, """{"message":"Not Found"}""")
            } else {
                jsonResponse(req, 200, releasesListJson)
            }
        }

        val service = GitHubReleaseService(client = client)
        val info = service.fetchLatestRelease("1.8.0", 13L)
        assertNotNull("Must succeed via fallback releases list", info)
        assertEquals("v1.9.0", info!!.tag)
        assertEquals("LaterBox-release.apk", info.apkFileName)
    }

    // =========================================================================
    // SCENARIO 4: Version comparison: installed version equals latest version
    // =========================================================================
    @Test
    fun testVersionComparison_InstalledEqualsLatest() {
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "app-release.apk", "size": 1000, "browser_download_url": "https://fake/app.apk" }
                ]
            }
        ]
        """.trimIndent()

        val info = defaultService.parseReleasesJson(mockJson, "1.9.0", 14L)
        assertNotNull(info)
        assertFalse("When versions match, isNewer must be false", info!!.isNewer)
    }

    // =========================================================================
    // SCENARIO 5: Version comparison: newer version available
    // =========================================================================
    @Test
    fun testVersionComparison_NewerVersionAvailable() {
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "app-release.apk", "size": 1000, "browser_download_url": "https://fake/app.apk" }
                ]
            }
        ]
        """.trimIndent()

        val info = defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
        assertNotNull(info)
        assertTrue("When remote tag is v1.9.0 and local is 1.8.0, isNewer must be true", info!!.isNewer)
    }

    // =========================================================================
    // SCENARIO 6: Version comparison: installed version newer than latest
    // =========================================================================
    @Test
    fun testVersionComparison_InstalledNewerThanLatest() {
        val mockJson = """
        [
            {
                "tag_name": "v1.8.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "app-release.apk", "size": 1000, "browser_download_url": "https://fake/app.apk" }
                ]
            }
        ]
        """.trimIndent()

        val info = defaultService.parseReleasesJson(mockJson, "1.9.0", 14L)
        assertNotNull(info)
        assertFalse("When installed is 1.9.0 and remote is v1.8.0, isNewer must be false", info!!.isNewer)
    }

    // =========================================================================
    // SCENARIO 7: Multiple APK assets: prioritizes release APK over debug APK
    // =========================================================================
    @Test
    fun testSelectsReleaseApkOverDebugApk() {
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "app-debug.apk", "size": 25000000, "browser_download_url": "https://fake/app-debug.apk" },
                    { "name": "app-release.apk", "size": 18000000, "browser_download_url": "https://fake/app-release.apk" }
                ]
            }
        ]
        """.trimIndent()

        val info = defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
        assertNotNull(info)
        assertEquals("app-release.apk", info!!.apkFileName)
        assertEquals("https://fake/app-release.apk", info.apkUrl)
    }

    // =========================================================================
    // SCENARIO 8: Only debug APK present throws NoCompatibleApk
    // =========================================================================
    @Test
    fun testOnlyDebugApkPresent_ThrowsNoCompatibleApk() {
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "app-debug.apk", "size": 25000000, "browser_download_url": "https://fake/app-debug.apk" }
                ]
            }
        ]
        """.trimIndent()

        try {
            defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
            fail("Expected UpdateCheckException.NoCompatibleApk")
        } catch (e: UpdateCheckException.NoCompatibleApk) {
            assertTrue(e.message!!.contains("compatible APK"))
        }
    }

    // =========================================================================
    // SCENARIO 9: Non-APK assets ignored
    // =========================================================================
    @Test
    fun testNonApkAssetsIgnored() {
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "source.tar.gz", "size": 50000, "browser_download_url": "https://fake/source.tar.gz" },
                    { "name": "checksums.txt", "size": 200, "browser_download_url": "https://fake/checksums.txt" },
                    { "name": "LaterBox-v1.9.0.apk", "size": 17000000, "browser_download_url": "https://fake/LaterBox-v1.9.0.apk" }
                ]
            }
        ]
        """.trimIndent()

        val info = defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
        assertNotNull(info)
        assertEquals("LaterBox-v1.9.0.apk", info!!.apkFileName)
    }

    // =========================================================================
    // SCENARIO 10: Release with no compatible APK throws NoCompatibleApk
    // =========================================================================
    @Test
    fun testMissingApkAsset_ThrowsNoCompatibleApk() {
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "source.tar.gz", "size": 100000, "browser_download_url": "https://fake/source.tar.gz" }
                ]
            }
        ]
        """.trimIndent()

        try {
            defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
            fail("Expected UpdateCheckException.NoCompatibleApk")
        } catch (e: UpdateCheckException.NoCompatibleApk) {
            assertTrue(e.message!!.contains("compatible APK"))
        }
    }

    // =========================================================================
    // SCENARIO 11: Empty releases list throws NoStableRelease
    // =========================================================================
    @Test
    fun testEmptyReleasesList_ThrowsNoStableRelease() {
        val mockJson = "[]"
        try {
            defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
            fail("Expected UpdateCheckException.NoStableRelease")
        } catch (e: UpdateCheckException.NoStableRelease) {
            assertTrue(e.message!!.contains("No published stable release"))
        }
    }

    // =========================================================================
    // SCENARIO 12: Only drafts and prereleases throws NoStableRelease
    // =========================================================================
    @Test
    fun testOnlyDraftsAndPrereleases_ThrowsNoStableRelease() {
        val mockJson = """
        [
            {
                "tag_name": "v2.0.0-alpha",
                "draft": false,
                "prerelease": true,
                "assets": [
                    { "name": "app-release.apk", "size": 1000, "browser_download_url": "https://fake/app.apk" }
                ]
            },
            {
                "tag_name": "v1.9.0-draft",
                "draft": true,
                "prerelease": false,
                "assets": [
                    { "name": "app-release.apk", "size": 1000, "browser_download_url": "https://fake/app.apk" }
                ]
            }
        ]
        """.trimIndent()

        try {
            defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
            fail("Expected UpdateCheckException.NoStableRelease")
        } catch (e: UpdateCheckException.NoStableRelease) {
            assertTrue(e.message!!.contains("No published stable release"))
        }
    }

    // =========================================================================
    // SCENARIO 13: Invalid JSON throws InvalidData
    // =========================================================================
    @Test
    fun testInvalidJson_ThrowsInvalidData() {
        val client = mockClient { req ->
            jsonResponse(req, 200, "<html><body>Internal Error</body></html>")
        }
        val service = GitHubReleaseService(client = client)

        try {
            service.fetchLatestRelease("1.8.0", 13L)
            fail("Expected UpdateCheckException.InvalidData")
        } catch (e: UpdateCheckException.InvalidData) {
            assertTrue(e.message!!.contains("Invalid JSON"))
        }
    }

    // =========================================================================
    // SCENARIO 14: HTTP 404 on both latest and releases list throws RepoNotFound
    // =========================================================================
    @Test
    fun testRepoNotFound_ThrowsRepoNotFound() {
        val client = mockClient { req ->
            jsonResponse(req, 404, """{"message":"Not Found"}""")
        }
        val service = GitHubReleaseService(client = client)

        try {
            service.fetchLatestRelease("1.8.0", 13L)
            fail("Expected UpdateCheckException.RepoNotFound")
        } catch (e: UpdateCheckException.RepoNotFound) {
            assertTrue(e.message!!.contains("could not be found"))
        }
    }

    // =========================================================================
    // SCENARIO 15: HTTP 403 Rate limit exceeded throws RateLimited
    // =========================================================================
    @Test
    fun testRateLimited_ThrowsRateLimited() {
        val client = mockClient { req ->
            jsonResponse(
                req,
                403,
                """{"message":"API rate limit exceeded"}""",
                headers = mapOf("x-ratelimit-remaining" to "0")
            )
        }
        val service = GitHubReleaseService(client = client)

        try {
            service.fetchLatestRelease("1.8.0", 13L)
            fail("Expected UpdateCheckException.RateLimited")
        } catch (e: UpdateCheckException.RateLimited) {
            assertTrue(e.message!!.contains("rate") || e.message!!.contains("limit"))
        }
    }

    // =========================================================================
    // SCENARIO 16: HTTP 500 / 503 Server error throws ServerUnavailable
    // =========================================================================
    @Test
    fun testServerError_ThrowsServerUnavailable() {
        val client = mockClient { req ->
            jsonResponse(req, 503, """{"message":"Service Unavailable"}""")
        }
        val service = GitHubReleaseService(client = client)

        try {
            service.fetchLatestRelease("1.8.0", 13L)
            fail("Expected UpdateCheckException.ServerUnavailable")
        } catch (e: UpdateCheckException.ServerUnavailable) {
            assertTrue(e.message!!.contains("unavailable"))
        }
    }

    // =========================================================================
    // SCENARIO 17: Offline network failure throws Offline
    // =========================================================================
    @Test
    fun testOfflineNetworkFailure_ThrowsOffline() {
        val client = mockClient {
            throw UnknownHostException("Unable to resolve host api.github.com")
        }
        val service = GitHubReleaseService(client = client)

        try {
            service.fetchLatestRelease("1.8.0", 13L)
            fail("Expected UpdateCheckException.Offline")
        } catch (e: UpdateCheckException.Offline) {
            assertTrue(e.message!!.contains("offline"))
        }
    }

    // =========================================================================
    // SCENARIO 18: Network timeout throws Timeout
    // =========================================================================
    @Test
    fun testTimeout_ThrowsTimeout() {
        val client = mockClient {
            throw SocketTimeoutException("Read timed out")
        }
        val service = GitHubReleaseService(client = client)

        try {
            service.fetchLatestRelease("1.8.0", 13L)
            fail("Expected UpdateCheckException.Timeout")
        } catch (e: UpdateCheckException.Timeout) {
            assertTrue(e.message!!.contains("timed out"))
        }
    }

    // =========================================================================
    // SCENARIO 19: SHA-256 Checksum extraction from asset digest
    // =========================================================================
    @Test
    fun testExtractSha256_FromAssetDigest() {
        val digestSha = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "body": "Release without hash in body",
                "assets": [
                    {
                        "name": "app-release.apk",
                        "size": 1000,
                        "digest": "sha256:$digestSha",
                        "browser_download_url": "https://fake/app.apk"
                    }
                ]
            }
        ]
        """.trimIndent()

        val info = defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
        assertNotNull(info)
        assertEquals(digestSha, info!!.expectedSha256)
    }

    // =========================================================================
    // SCENARIO 20: SHA-256 Checksum extraction from release notes
    // =========================================================================
    @Test
    fun testExtractSha256_FromReleaseNotes() {
        val bodySha = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        val mockJson = """
        [
            {
                "tag_name": "v1.9.0",
                "draft": false,
                "prerelease": false,
                "body": "Verification:\nSHA-256: $bodySha\nEnjoy Later Box!",
                "assets": [
                    {
                        "name": "app-release.apk",
                        "size": 1000,
                        "browser_download_url": "https://fake/app.apk"
                    }
                ]
            }
        ]
        """.trimIndent()

        val info = defaultService.parseReleasesJson(mockJson, "1.8.0", 13L)
        assertNotNull(info)
        assertEquals(bodySha, info!!.expectedSha256)
    }
}
