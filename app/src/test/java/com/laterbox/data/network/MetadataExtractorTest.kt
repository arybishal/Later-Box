package com.laterbox.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MetadataExtractorTest {

    @Test
    fun testExtractUrlFromText() {
        val raw1 = "Check out this reel: https://www.instagram.com/reel/C2xYZ/ it's awesome!"
        val extracted1 = MetadataExtractor.extractUrlFromText(raw1)
        assertNotNull(extracted1)
        assertEquals("https://www.instagram.com/reel/C2xYZ/", extracted1)

        val raw2 = "https://twitter.com/user/status/9876543210"
        val extracted2 = MetadataExtractor.extractUrlFromText(raw2)
        assertEquals("https://twitter.com/user/status/9876543210", extracted2)

        val raw3 = "Just a quick reminder to buy groceries"
        val extracted3 = MetadataExtractor.extractUrlFromText(raw3)
        assertNull(extracted3)
    }

    @Test
    fun testExtractDomain() {
        assertEquals("instagram.com", MetadataExtractor.extractDomain("https://www.instagram.com/p/12345/"))
        assertEquals("github.com", MetadataExtractor.extractDomain("https://github.com/arybishal/Later-Box"))
        assertEquals("nytimes.com", MetadataExtractor.extractDomain("https://www.nytimes.com/2026/11/09/technology/article.html"))
        assertEquals("", MetadataExtractor.extractDomain("invalid-url"))
    }
}
