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

        val raw2 = "https://twitter.com/user/status/9876543210."
        val extracted2 = MetadataExtractor.extractUrlFromText(raw2)
        assertEquals("https://twitter.com/user/status/9876543210", extracted2)

        val raw3 = "Just a quick reminder to buy groceries"
        val extracted3 = MetadataExtractor.extractUrlFromText(raw3)
        assertNull(extracted3)

        val raw4 = "Here is the link (https://github.com/arybishal/Later-Box)"
        val extracted4 = MetadataExtractor.extractUrlFromText(raw4)
        assertEquals("https://github.com/arybishal/Later-Box", extracted4)
    }

    @Test
    fun testExtractDomain() {
        assertEquals("instagram.com", MetadataExtractor.extractDomain("https://www.instagram.com/p/12345/"))
        assertEquals("github.com", MetadataExtractor.extractDomain("https://github.com/arybishal/Later-Box"))
        assertEquals("nytimes.com", MetadataExtractor.extractDomain("https://www.nytimes.com/2026/11/09/technology/article.html"))
        assertEquals("", MetadataExtractor.extractDomain("invalid-url"))
    }

    @Test
    fun testExtractYouTubeVideoId() {
        assertEquals(
            "dQw4w9WgXcQ",
            MetadataExtractor.extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        )
        assertEquals(
            "dQw4w9WgXcQ",
            MetadataExtractor.extractYouTubeVideoId("https://youtu.be/dQw4w9WgXcQ")
        )
        assertEquals(
            "dQw4w9WgXcQ",
            MetadataExtractor.extractYouTubeVideoId("https://www.youtube.com/embed/dQw4w9WgXcQ")
        )
        assertEquals(
            "12345678901",
            MetadataExtractor.extractYouTubeVideoId("https://www.youtube.com/shorts/12345678901")
        )
        assertNull(
            MetadataExtractor.extractYouTubeVideoId("https://vimeo.com/123456")
        )
    }

    @Test
    fun testDetectSourceApp() {
        assertEquals("YouTube", MetadataExtractor.detectSourceApp("youtube.com"))
        assertEquals("YouTube", MetadataExtractor.detectSourceApp("youtu.be"))
        assertEquals("Instagram", MetadataExtractor.detectSourceApp("instagram.com"))
        assertEquals("TikTok", MetadataExtractor.detectSourceApp("tiktok.com"))
        assertEquals("X (Twitter)", MetadataExtractor.detectSourceApp("x.com"))
        assertEquals("X (Twitter)", MetadataExtractor.detectSourceApp("twitter.com"))
        assertEquals("Reddit", MetadataExtractor.detectSourceApp("reddit.com"))
        assertEquals("Facebook", MetadataExtractor.detectSourceApp("facebook.com"))
        assertEquals("Medium", MetadataExtractor.detectSourceApp("medium.com"))
        assertEquals("Substack", MetadataExtractor.detectSourceApp("substack.com"))
        assertEquals("GitHub", MetadataExtractor.detectSourceApp("github.com"))
    }
}
