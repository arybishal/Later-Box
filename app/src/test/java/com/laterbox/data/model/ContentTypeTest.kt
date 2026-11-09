package com.laterbox.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentTypeTest {

    @Test
    fun testFromMimeType() {
        assertEquals(ContentType.IMAGE, ContentType.fromMimeOrUrl("image/jpeg", "content://media/1"))
        assertEquals(ContentType.IMAGE, ContentType.fromMimeOrUrl("image/png", "content://media/2"))
        assertEquals(ContentType.VIDEO, ContentType.fromMimeOrUrl("video/mp4", "content://media/3"))
        assertEquals(ContentType.AUDIO, ContentType.fromMimeOrUrl("audio/mpeg", "content://media/4"))
        assertEquals(ContentType.DOCUMENT, ContentType.fromMimeOrUrl("application/pdf", "content://media/5"))
        assertEquals(ContentType.NOTE, ContentType.fromMimeOrUrl("text/plain", "Simple note text"))
    }

    @Test
    fun testFromUrlPattern() {
        assertEquals(ContentType.VIDEO, ContentType.fromMimeOrUrl(null, "https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals(ContentType.VIDEO, ContentType.fromMimeOrUrl(null, "https://youtu.be/dQw4w9WgXcQ"))
        assertEquals(ContentType.VIDEO, ContentType.fromMimeOrUrl(null, "https://www.tiktok.com/@user/video/12345"))
        assertEquals(ContentType.IMAGE, ContentType.fromMimeOrUrl(null, "https://images.unsplash.com/photo-123.jpg"))
        assertEquals(ContentType.DOCUMENT, ContentType.fromMimeOrUrl(null, "https://arxiv.org/pdf/2401.12345.pdf"))
        assertEquals(ContentType.ARTICLE, ContentType.fromMimeOrUrl(null, "https://medium.com/@author/modern-android-architecture"))
        assertEquals(ContentType.LINK, ContentType.fromMimeOrUrl(null, "https://github.com/arybishal/Later-Box"))
    }
}
