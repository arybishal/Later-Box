package com.laterbox.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class BackupManagerTest {

    @Test
    fun testBackupManifestDataClass() {
        val manifest = BackupManifest(
            schemaVersion = 1,
            appVersion = "1.2.0",
            appVersionCode = 4,
            createdAt = 1731174000000L,
            totalItems = 42,
            totalPlaylists = 5,
            totalMediaFiles = 12,
            estimatedBytes = 1048576L,
            sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )

        assertEquals(1, manifest.schemaVersion)
        assertEquals("1.2.0", manifest.appVersion)
        assertEquals(4, manifest.appVersionCode)
        assertEquals(1731174000000L, manifest.createdAt)
        assertEquals(42, manifest.totalItems)
        assertEquals(5, manifest.totalPlaylists)
        assertEquals(12, manifest.totalMediaFiles)
        assertEquals(1048576L, manifest.estimatedBytes)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", manifest.sha256Checksum)

        val summary = BackupSummary(
            totalItems = manifest.totalItems,
            totalPlaylists = manifest.totalPlaylists,
            totalMediaFiles = manifest.totalMediaFiles,
            estimatedSizeBytes = manifest.estimatedBytes
        )
        assertEquals(42, summary.totalItems)
        assertEquals(5, summary.totalPlaylists)
        assertEquals(12, summary.totalMediaFiles)
        assertEquals(1048576L, summary.estimatedSizeBytes)
    }

    @Test
    fun testSha256ChecksumVerification() {
        val testData = "Later Box Portable Backup Test Content".toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(testData).joinToString("") { "%02x".format(it) }

        assertEquals(64, hash.length)

        // Verify deterministic hashing
        val digest2 = MessageDigest.getInstance("SHA-256")
        val hash2 = digest2.digest(testData).joinToString("") { "%02x".format(it) }
        assertEquals(hash, hash2)
    }

    @Test
    fun testZipSlipPathTraversalRejection() {
        val baseDestDir = File(System.getProperty("java.io.tmpdir"), "laterbox_test_zipslip")
        baseDestDir.mkdirs()

        fun isSafePath(entryName: String, baseDir: File): Boolean {
            val destinationFile = File(baseDir, entryName)
            val canonicalPath = destinationFile.canonicalPath
            val canonicalBase = baseDir.canonicalPath + File.separator
            return canonicalPath.startsWith(canonicalBase)
        }

        // Valid relative paths inside media folder
        assertTrue(isSafePath("media/cover_123.jpg", baseDestDir))
        assertTrue(isSafePath("manifest.json", baseDestDir))

        // Malicious paths attempting directory traversal (Zip Slip vulnerability)
        assertFalse(isSafePath("../../system/etc/evil.sh", baseDestDir))
        assertFalse(isSafePath("media/../../../evil.txt", baseDestDir))
        assertFalse(isSafePath("../outer.bin", baseDestDir))

        baseDestDir.deleteRecursively()
    }
}
