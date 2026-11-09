package com.laterbox.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialLinkResolverTest {

    @Test
    fun testUrlSanitization() {
        assertEquals("https://instagram.com/p/12345", SocialLinkResolver.sanitizeUrl("  instagram.com/p/12345  "))
        assertEquals("https://twitter.com/user/status/123", SocialLinkResolver.sanitizeUrl("twitter.com/user/status/123"))
        assertEquals("http://example.com", SocialLinkResolver.sanitizeUrl("http://example.com"))
        assertEquals("https://youtube.com/watch?v=abc", SocialLinkResolver.sanitizeUrl("https://youtube.com/watch?v=abc"))
    }

    @Test
    fun testInstagramDetection() {
        val urls = listOf(
            "https://www.instagram.com/p/DA12345/",
            "https://instagram.com/reel/C890123/",
            "http://instagr.am/p/xyz123",
            "instagram.com/stories/user/12345"
        )
        for (url in urls) {
            assertEquals(SocialPlatform.INSTAGRAM, SocialLinkResolver.detectPlatform(url))
            assertTrue(SocialLinkResolver.isSupportedSocialUrl(url))
        }
    }

    @Test
    fun testFacebookDetection() {
        val urls = listOf(
            "https://www.facebook.com/user/posts/123",
            "https://facebook.com/watch/?v=456",
            "https://fb.watch/abcdef/",
            "https://m.facebook.com/story.php?story_fbid=123",
            "fb.com/groups/12345"
        )
        for (url in urls) {
            assertEquals(SocialPlatform.FACEBOOK, SocialLinkResolver.detectPlatform(url))
            assertTrue(SocialLinkResolver.isSupportedSocialUrl(url))
        }
    }

    @Test
    fun testTwitterXDetection() {
        val urls = listOf(
            "https://x.com/AndroidDev/status/185000000000",
            "https://www.x.com/news/status/123",
            "https://twitter.com/jack/status/20",
            "https://t.co/xyz123",
            "mobile.twitter.com/i/web/status/123"
        )
        for (url in urls) {
            assertEquals(SocialPlatform.TWITTER_X, SocialLinkResolver.detectPlatform(url))
            assertTrue(SocialLinkResolver.isSupportedSocialUrl(url))
        }
    }

    @Test
    fun testTikTokDetection() {
        val urls = listOf(
            "https://www.tiktok.com/@creator/video/7123456789",
            "https://tiktok.com/@creator/video/123",
            "https://vm.tiktok.com/ZM8abcde/",
            "https://vt.tiktok.com/ZS8fghij/"
        )
        for (url in urls) {
            assertEquals(SocialPlatform.TIKTOK, SocialLinkResolver.detectPlatform(url))
            assertTrue(SocialLinkResolver.isSupportedSocialUrl(url))
        }
    }

    @Test
    fun testYouTubeDetection() {
        val urls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/shorts/abcdef12345",
            "https://m.youtube.com/watch?v=test",
            "https://youtu.be/dQw4w9WgXcQ?t=10"
        )
        for (url in urls) {
            assertEquals(SocialPlatform.YOUTUBE, SocialLinkResolver.detectPlatform(url))
            assertTrue(SocialLinkResolver.isSupportedSocialUrl(url))
        }
    }

    @Test
    fun testRedditDetection() {
        val urls = listOf(
            "https://www.reddit.com/r/androiddev/comments/12345/later_box/",
            "https://reddit.com/r/technology/",
            "https://redd.it/12345",
            "https://old.reddit.com/r/kotlin"
        )
        for (url in urls) {
            assertEquals(SocialPlatform.REDDIT, SocialLinkResolver.detectPlatform(url))
            assertTrue(SocialLinkResolver.isSupportedSocialUrl(url))
        }
    }

    @Test
    fun testGenericWebFallback() {
        val urls = listOf(
            "https://github.com/arybishal/Later-Box",
            "https://medium.com/@dev/modern-android",
            "https://en.wikipedia.org/wiki/Kotlin",
            "https://news.ycombinator.com"
        )
        for (url in urls) {
            assertEquals(SocialPlatform.GENERIC_WEB, SocialLinkResolver.detectPlatform(url))
            assertFalse(SocialLinkResolver.isSupportedSocialUrl(url))
        }
    }

    @Test
    fun testCandidatePackages() {
        val igPackages = SocialLinkResolver.getCandidatePackages(SocialPlatform.INSTAGRAM)
        assertTrue(igPackages.contains("com.instagram.android"))

        val fbPackages = SocialLinkResolver.getCandidatePackages(SocialPlatform.FACEBOOK)
        assertTrue(fbPackages.contains("com.facebook.katana"))
        assertTrue(fbPackages.contains("com.facebook.lite"))

        val xPackages = SocialLinkResolver.getCandidatePackages(SocialPlatform.TWITTER_X)
        assertTrue(xPackages.contains("com.twitter.android"))

        val ytPackages = SocialLinkResolver.getCandidatePackages(SocialPlatform.YOUTUBE)
        assertTrue(ytPackages.contains("com.google.android.youtube"))

        val ttPackages = SocialLinkResolver.getCandidatePackages(SocialPlatform.TIKTOK)
        assertTrue(ttPackages.contains("com.zhiliaoapp.musically"))

        val redditPackages = SocialLinkResolver.getCandidatePackages(SocialPlatform.REDDIT)
        assertTrue(redditPackages.contains("com.reddit.frontpage"))
    }
}
