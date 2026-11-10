package com.laterbox.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticVersionTest {

    @Test
    fun testParseSimpleVersion() {
        val version = SemanticVersion.parse("1.3.1")
        assertNotNull(version)
        assertEquals(1, version!!.major)
        assertEquals(3, version.minor)
        assertEquals(1, version.patch)
        assertNull(version.suffix)
    }

    @Test
    fun testParsePrefixV() {
        val version = SemanticVersion.parse("v1.3.1")
        assertNotNull(version)
        assertEquals(1, version!!.major)
        assertEquals(3, version.minor)
        assertEquals(1, version.patch)
    }

    @Test
    fun testParseWithSuffix() {
        val version = SemanticVersion.parse("v2.0.0-beta1")
        assertNotNull(version)
        assertEquals(2, version!!.major)
        assertEquals(0, version.minor)
        assertEquals(0, version.patch)
        assertEquals("beta1", version.suffix)
    }

    @Test
    fun testSemanticComparisonNonLexicographical() {
        // "1.10.0" vs "1.9.0" — In lexicographical comparison, "1.10.0" < "1.9.0" which is WRONG.
        // In semantic integer comparison, 1.10.0 > 1.9.0 which is CORRECT.
        val v1_10 = SemanticVersion.parse("1.10.0")!!
        val v1_9 = SemanticVersion.parse("1.9.0")!!
        assertTrue("1.10.0 must be greater than 1.9.0", v1_10 > v1_9)
    }

    @Test
    fun testIsVersionNewer() {
        assertTrue(SemanticVersion.isVersionNewer("v1.3.1", "1.3.0"))
        assertTrue(SemanticVersion.isVersionNewer("v2.0.0", "1.99.9"))
        assertFalse(SemanticVersion.isVersionNewer("v1.3.1", "1.3.1"))
        assertFalse(SemanticVersion.isVersionNewer("v1.2.0", "1.3.0"))
    }

    @Test
    fun testStableReleaseBeatsSuffixRelease() {
        val stable = SemanticVersion.parse("1.3.0")!!
        val beta = SemanticVersion.parse("1.3.0-rc1")!!
        assertTrue("Stable release must be newer than pre-release with same base", stable > beta)
    }

    @Test
    fun testPointZeroZeroOneIncrements() {
        // Padded 3-segment format: 2.0.001, 2.0.002
        assertTrue(SemanticVersion.isVersionNewer("v2.0.001", "2.0.0"))
        assertTrue(SemanticVersion.isVersionNewer("2.0.002", "2.0.001"))
        assertFalse(SemanticVersion.isVersionNewer("2.0.001", "2.0.002"))

        // 4-segment dot format: 2.0.0.1, 2.0.0.2
        assertTrue(SemanticVersion.isVersionNewer("v2.0.0.1", "2.0.0"))
        assertTrue(SemanticVersion.isVersionNewer("2.0.0.2", "2.0.0.1"))
        assertFalse(SemanticVersion.isVersionNewer("2.0.0.1", "2.0.0.2"))
    }
}
