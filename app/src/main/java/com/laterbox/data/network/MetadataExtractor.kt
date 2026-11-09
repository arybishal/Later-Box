package com.laterbox.data.network

import com.laterbox.data.model.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ExtractedMetadata(
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val sourceDomain: String,
    val contentType: ContentType,
    val canonicalUrl: String
)

object MetadataExtractor {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val URL_REGEX = Pattern.compile(
        "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/[^\\s]*)?",
        Pattern.CASE_INSENSITIVE
    )

    fun extractUrlFromText(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val spaceIdx = trimmed.indexOfFirst { it.isWhitespace() }
            return if (spaceIdx != -1) trimmed.substring(0, spaceIdx) else trimmed
        }
        val matcher = URL_REGEX.matcher(text)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }

    fun extractDomain(url: String): String {
        return try {
            val uri = URI(url)
            val host = uri.host ?: ""
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun extractMetadata(rawUrl: String): ExtractedMetadata = withContext(Dispatchers.IO) {
        val domain = extractDomain(rawUrl)
        val defaultTitle = if (domain.isNotEmpty()) domain.replaceFirstChar { it.uppercase() } else "Saved Link"
        val detectedType = ContentType.fromMimeOrUrl(null, rawUrl)

        try {
            val request = Request.Builder()
                .url(rawUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                )
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) {
                    return@withContext ExtractedMetadata(
                        title = defaultTitle,
                        description = null,
                        thumbnailUrl = null,
                        sourceDomain = domain,
                        contentType = detectedType,
                        canonicalUrl = rawUrl
                    )
                }

                // Read at most first 256KB to quickly extract head meta tags
                val html = response.body!!.byteStream().bufferedReader().use { reader ->
                    val buffer = CharArray(262144)
                    val read = reader.read(buffer)
                    if (read > 0) String(buffer, 0, read) else ""
                }

                val title = findMetaTag(html, "og:title")
                    ?: findMetaTag(html, "twitter:title")
                    ?: findTitleTag(html)
                    ?: defaultTitle

                val description = findMetaTag(html, "og:description")
                    ?: findMetaTag(html, "twitter:description")
                    ?: findMetaTag(html, "description")

                var imageUrl = findMetaTag(html, "og:image")
                    ?: findMetaTag(html, "twitter:image")
                    ?: findFavicon(html, rawUrl)

                if (imageUrl != null && !imageUrl.startsWith("http")) {
                    imageUrl = resolveUrl(rawUrl, imageUrl)
                }

                ExtractedMetadata(
                    title = cleanHtmlEntities(title.trim()),
                    description = description?.let { cleanHtmlEntities(it.trim()) },
                    thumbnailUrl = imageUrl,
                    sourceDomain = domain,
                    contentType = detectedType,
                    canonicalUrl = rawUrl
                )
            }
        } catch (_: Exception) {
            // Graceful fallback on network timeout, SSL error or blocked requests
            ExtractedMetadata(
                title = defaultTitle,
                description = null,
                thumbnailUrl = null,
                sourceDomain = domain,
                contentType = detectedType,
                canonicalUrl = rawUrl
            )
        }
    }

    private fun findMetaTag(html: String, property: String): String? {
        val pattern1 = Pattern.compile(
            """<meta\s+[^>]*?(?:property|name)=["']$property["'][^>]*?content=["']([^"']+)["']""",
            Pattern.CASE_INSENSITIVE
        )
        val m1 = pattern1.matcher(html)
        if (m1.find()) return m1.group(1)

        val pattern2 = Pattern.compile(
            """<meta\s+[^>]*?content=["']([^"']+)["'][^>]*?(?:property|name)=["']$property["']""",
            Pattern.CASE_INSENSITIVE
        )
        val m2 = pattern2.matcher(html)
        if (m2.find()) return m2.group(1)

        return null
    }

    private fun findTitleTag(html: String): String? {
        val pattern = Pattern.compile("<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun findFavicon(html: String, baseUrl: String): String? {
        val pattern = Pattern.compile(
            """<link\s+[^>]*?rel=["'](?:shortcut\s+)?icon["'][^>]*?href=["']([^"']+)["']""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        return if (matcher.find()) {
            resolveUrl(baseUrl, matcher.group(1) ?: "")
        } else {
            val domain = extractDomain(baseUrl)
            if (domain.isNotEmpty()) "https://www.google.com/s2/favicons?domain=$domain&sz=128" else null
        }
    }

    private fun resolveUrl(baseUrl: String, relativeUrl: String): String {
        return try {
            val base = URI(baseUrl)
            base.resolve(relativeUrl).toString()
        } catch (_: Exception) {
            relativeUrl
        }
    }

    private fun cleanHtmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
    }
}
