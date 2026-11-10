package com.laterbox.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseServiceTest {

    private val service = GitHubReleaseService()

    @Test
    fun testParseStableReleaseWithApkAsset() {
        val mockJson = """
        [
            {
                "tag_name": "v1.4.0",
                "name": "Later Box v1.4.0 - Major Productivity Update",
                "body": "- Added In-App Updates\n- Reader Mode Enhancements",
                "draft": false,
                "prerelease": false,
                "published_at": "2026-11-10T12:00:00Z",
                "html_url": "https://github.com/arybishal/Later-Box/releases/tag/v1.4.0",
                "assets": [
                    {
                        "name": "LaterBox-v1.4.0-release.apk",
                        "size": 16000000,
                        "browser_download_url": "https://github.com/arybishal/Later-Box/releases/download/v1.4.0/LaterBox-v1.4.0-release.apk"
                    },
                    {
                        "name": "LaterBox-v1.4.0-debug.apk",
                        "size": 22000000,
                        "browser_download_url": "https://github.com/arybishal/Later-Box/releases/download/v1.4.0/LaterBox-v1.4.0-debug.apk"
                    }
                ]
            }
        ]
        """.trimIndent()

        val updateInfo = service.parseReleasesJson(
            jsonString = mockJson,
            currentVersionName = "1.3.1",
            currentVersionCode = 6L
        )

        assertNotNull(updateInfo)
        assertEquals("v1.4.0", updateInfo!!.tag)
        assertEquals("1.4.0", updateInfo.versionName)
        assertTrue("v1.4.0 must be recognized as newer than 1.3.1", updateInfo.isNewer)
        assertEquals("LaterBox-v1.4.0-release.apk", updateInfo.apkFileName)
        assertEquals(16000000L, updateInfo.apkSizeBytes)
        assertTrue(updateInfo.notes.contains("In-App Updates"))
    }

    @Test
    fun testPrereleasesAndDraftsIgnored() {
        val mockJson = """
        [
            {
                "tag_name": "v2.0.0-alpha",
                "name": "v2.0.0 Alpha",
                "draft": false,
                "prerelease": true,
                "assets": [
                    { "name": "app-release.apk", "size": 15000000, "browser_download_url": "https://fake/2.0.apk" }
                ]
            },
            {
                "tag_name": "v1.9.0-draft",
                "name": "v1.9.0 Draft",
                "draft": true,
                "prerelease": false,
                "assets": [
                    { "name": "app-release.apk", "size": 15000000, "browser_download_url": "https://fake/1.9.apk" }
                ]
            },
            {
                "tag_name": "v1.3.5",
                "name": "Later Box v1.3.5 Stable",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "LaterBox-v1.3.5-release.apk", "size": 15500000, "browser_download_url": "https://github.com/arybishal/Later-Box/releases/download/v1.3.5/LaterBox-v1.3.5-release.apk" }
                ]
            }
        ]
        """.trimIndent()

        val updateInfo = service.parseReleasesJson(mockJson, "1.3.1", 6L)

        assertNotNull(updateInfo)
        // Must skip the alpha prerelease and draft and select v1.3.5
        assertEquals("v1.3.5", updateInfo!!.tag)
        assertEquals("LaterBox-v1.3.5-release.apk", updateInfo.apkFileName)
    }

    @Test
    fun testNoNewerReleaseAvailable() {
        val mockJson = """
        [
            {
                "tag_name": "v1.3.1",
                "name": "Later Box v1.3.1",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "LaterBox-v1.3.1-release.apk", "size": 15500000, "browser_download_url": "https://fake/1.3.1.apk" }
                ]
            }
        ]
        """.trimIndent()

        val updateInfo = service.parseReleasesJson(mockJson, "1.3.1", 6L)

        assertNotNull(updateInfo)
        assertFalse("Current installed version 1.3.1 is not older than 1.3.1", updateInfo!!.isNewer)
    }

    @Test
    fun testMissingApkAssetReturnsNull() {
        val mockJson = """
        [
            {
                "tag_name": "v1.5.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "source.tar.gz", "size": 100000, "browser_download_url": "https://fake/source.tar.gz" }
                ]
            }
        ]
        """.trimIndent()

        val updateInfo = service.parseReleasesJson(mockJson, "1.3.1", 6L)
        assertNull("Release with no APK asset must return null", updateInfo)
    }

    @Test
    fun testSelectsReleaseApkOverDebugApk() {
        val mockJson = """
        [
            {
                "tag_name": "v1.4.0",
                "draft": false,
                "prerelease": false,
                "assets": [
                    { "name": "LaterBox-v1.4.0-debug.apk", "size": 22000000, "browser_download_url": "https://fake/debug.apk" },
                    { "name": "LaterBox-v1.4.0-release.apk", "size": 15500000, "browser_download_url": "https://fake/release.apk" }
                ]
            }
        ]
        """.trimIndent()

        val updateInfo = service.parseReleasesJson(mockJson, "1.3.1", 6L)
        assertNotNull(updateInfo)
        assertEquals("LaterBox-v1.4.0-release.apk", updateInfo!!.apkFileName)
        assertEquals("https://fake/release.apk", updateInfo.apkUrl)
    }
}
