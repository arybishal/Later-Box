package com.laterbox.data.model

enum class ContentType(val displayName: String) {
    ARTICLE("Article"),
    VIDEO("Video"),
    IMAGE("Image"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    LINK("Link"),
    NOTE("Note");

    companion object {
        fun fromMimeOrUrl(mimeType: String?, url: String): ContentType {
            if (mimeType != null) {
                when {
                    mimeType.startsWith("image/") -> return IMAGE
                    mimeType.startsWith("video/") -> return VIDEO
                    mimeType.startsWith("audio/") -> return AUDIO
                    mimeType.contains("pdf") || mimeType.contains("document") -> return DOCUMENT
                    mimeType == "text/plain" && !url.startsWith("http") -> return NOTE
                }
            }
            val lower = url.lowercase()
            return when {
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif") -> IMAGE
                lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm") || lower.contains("youtube.com") || lower.contains("youtu.be") || lower.contains("tiktok.com") -> VIDEO
                lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".wav") || lower.endsWith(".ogg") -> AUDIO
                lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".epub") -> DOCUMENT
                lower.contains("medium.com") || lower.contains("substack.com") || lower.contains("blog") || lower.contains("article") || lower.contains("news") -> ARTICLE
                lower.startsWith("http://") || lower.startsWith("https://") -> LINK
                else -> NOTE
            }
        }
    }
}

enum class DownloadStatus {
    NONE,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    TITLE_AZ("Title (A-Z)"),
    LAST_OPENED("Recently Opened")
}

enum class ViewMode {
    GRID,
    LIST
}

enum class ThemeMode(val label: String) {
    SYSTEM("Follow System"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}
