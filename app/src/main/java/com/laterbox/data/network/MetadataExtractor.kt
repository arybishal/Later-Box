package com.laterbox.data.network

import com.laterbox.data.model.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ExtractedMetadata(
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val sourceDomain: String,
    val contentType: ContentType,
    val canonicalUrl: String,
    val sourceApp: String? = null
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

    private val YOUTUBE_REGEX = Pattern.compile(
        """(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?|shorts)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})""",
        Pattern.CASE_INSENSITIVE
    )

    fun extractUrlFromText(text: String): String? {
        val trimmed = text.trim()
        val raw = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            val spaceIdx = trimmed.indexOfFirst { it.isWhitespace() }
            if (spaceIdx != -1) trimmed.substring(0, spaceIdx) else trimmed
        } else {
            val matcher = URL_REGEX.matcher(text)
            if (matcher.find()) matcher.group() else null
        }

        // Clean trailing punctuation
        return raw?.trimEnd('.', ',', ')', ']', '}', ';', '!', '"', '\'')
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

    fun extractYouTubeVideoId(url: String): String? {
        val matcher = YOUTUBE_REGEX.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    fun detectSourceApp(domain: String): String {
        val lower = domain.lowercase()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("instagram.com") -> "Instagram"
            lower.contains("tiktok.com") -> "TikTok"
            lower.contains("twitter.com") || lower.contains("x.com") -> "X (Twitter)"
            lower.contains("reddit.com") -> "Reddit"
            lower.contains("facebook.com") || lower.contains("fb.watch") -> "Facebook"
            lower.contains("medium.com") -> "Medium"
            lower.contains("substack.com") -> "Substack"
            lower.contains("github.com") -> "GitHub"
            else -> if (domain.isNotEmpty()) domain.replaceFirstChar { it.uppercase() } else "Web"
        }
    }

    suspend fun extractMetadata(rawUrl: String): ExtractedMetadata = withContext(Dispatchers.IO) {
        val cleanUrl = extractUrlFromText(rawUrl) ?: rawUrl
        val domain = extractDomain(cleanUrl)
        val sourceApp = detectSourceApp(domain)
        val defaultTitle = sourceApp
        val detectedType = ContentType.fromMimeOrUrl(null, cleanUrl)

        // 1. Specialized YouTube Handler (zero-scrape reliability)
        val ytVideoId = extractYouTubeVideoId(cleanUrl)
        if (ytVideoId != null) {
            val ytThumbnail = "https://img.youtube.com/vi/$ytVideoId/hqdefault.jpg"
            val oembedTitle = fetchOEmbedTitle("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$ytVideoId&format=json")
            return@withContext ExtractedMetadata(
                title = oembedTitle ?: "YouTube Video ($ytVideoId)",
                description = "Watch on YouTube",
                thumbnailUrl = ytThumbnail,
                sourceDomain = "youtube.com",
                contentType = ContentType.VIDEO,
                canonicalUrl = "https://www.youtube.com/watch?v=$ytVideoId",
                sourceApp = "YouTube"
            )
        }

        // 2. Specialized Twitter / X oEmbed Handler
        if (domain.contains("twitter.com") || domain.contains("x.com")) {
            val tweetTitle = fetchOEmbedTitle("https://publish.twitter.com/oembed?url=${URLEncoder.encode(cleanUrl, "UTF-8")}")
            if (tweetTitle != null) {
                return@withContext ExtractedMetadata(
                    title = tweetTitle,
                    description = "Post on X",
                    thumbnailUrl = null,
                    sourceDomain = domain,
                    contentType = ContentType.LINK,
                    canonicalUrl = cleanUrl,
                    sourceApp = "X (Twitter)"
                )
            }
        }

        // 3. Specialized Reddit oEmbed Handler
        if (domain.contains("reddit.com")) {
            val redditTitle = fetchOEmbedTitle("https://www.reddit.com/oembed?url=${URLEncoder.encode(cleanUrl, "UTF-8")}")
            if (redditTitle != null) {
                return@withContext ExtractedMetadata(
                    title = redditTitle,
                    description = "Reddit Discussion",
                    thumbnailUrl = null,
                    sourceDomain = domain,
                    contentType = ContentType.ARTICLE,
                    canonicalUrl = cleanUrl,
                    sourceApp = "Reddit"
                )
            }
        }

        // 4. Standard OpenGraph HTML Scraper
        try {
            val request = Request.Builder()
                .url(cleanUrl)
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
                        canonicalUrl = cleanUrl,
                        sourceApp = sourceApp
                    )
                }

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
                    ?: findFavicon(html, cleanUrl)

                if (imageUrl != null && !imageUrl.startsWith("http")) {
                    imageUrl = resolveUrl(cleanUrl, imageUrl)
                }

                ExtractedMetadata(
                    title = cleanHtmlEntities(title.trim()),
                    description = description?.let { cleanHtmlEntities(it.trim()) },
                    thumbnailUrl = imageUrl,
                    sourceDomain = domain,
                    contentType = detectedType,
                    canonicalUrl = cleanUrl,
                    sourceApp = sourceApp
                )
            }
        } catch (_: Exception) {
            ExtractedMetadata(
                title = defaultTitle,
                description = null,
                thumbnailUrl = null,
                sourceDomain = domain,
                contentType = detectedType,
                canonicalUrl = cleanUrl,
                sourceApp = sourceApp
            )
        }
    }

    private fun fetchOEmbedTitle(oembedUrl: String): String? {
        return try {
            val request = Request.Builder().url(oembedUrl).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful || response.body == null) return null
                val json = response.body!!.string()
                val titlePattern = Pattern.compile(""""title"\s*:\s*"([^"]+)"""")
                val matcher = titlePattern.matcher(json)
                if (matcher.find()) cleanHtmlEntities(matcher.group(1) ?: "") else null
            }
        } catch (_: Exception) {
            null
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
            .replace("\\u0026", "&")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
    }
}
