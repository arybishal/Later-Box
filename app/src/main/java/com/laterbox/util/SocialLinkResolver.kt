package com.laterbox.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

enum class SocialPlatform(val displayName: String) {
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook"),
    TWITTER_X("X (Twitter)"),
    TIKTOK("TikTok"),
    YOUTUBE("YouTube"),
    REDDIT("Reddit"),
    GENERIC_WEB("Web Browser")
}

object SocialLinkResolver {

    private val BROWSER_PACKAGE_NAMES = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.google.android.apps.chrome",
        "com.sec.android.app.sbrowser",
        "com.microsoft.emmx",
        "com.opera.browser",
        "com.brave.browser",
        "com.duckduckgo.mobile.android",
        "com.android.browser"
    )

    fun sanitizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    /**
     * Extracts host safely across both Android runtime and host JVM unit tests
     * without relying exclusively on android.net.Uri stubs.
     */
    fun extractHost(rawUrl: String): String? {
        val sanitized = sanitizeUrl(rawUrl)
        return try {
            val javaUri = java.net.URI(sanitized)
            javaUri.host?.lowercase()?.ifEmpty { null }
        } catch (_: Exception) {
            try {
                val afterScheme = if (sanitized.contains("://")) sanitized.substringAfter("://") else sanitized
                val hostPart = afterScheme.substringBefore('/').substringBefore('?').substringBefore(':')
                hostPart.lowercase().ifEmpty { null }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun parseUri(rawUrl: String): Uri? {
        return try {
            val sanitized = sanitizeUrl(rawUrl)
            val uri = Uri.parse(sanitized)
            if (uri.scheme.isNullOrBlank() || uri.host.isNullOrBlank()) null else uri
        } catch (_: Exception) {
            null
        }
    }

    fun detectPlatform(rawUrl: String): SocialPlatform {
        val host = extractHost(rawUrl) ?: return SocialPlatform.GENERIC_WEB

        return when {
            host == "instagram.com" || host.endsWith(".instagram.com") || host == "instagr.am" ->
                SocialPlatform.INSTAGRAM

            host == "facebook.com" || host.endsWith(".facebook.com") ||
                    host == "fb.watch" || host == "fb.com" || host.endsWith(".fb.com") ->
                SocialPlatform.FACEBOOK

            host == "twitter.com" || host.endsWith(".twitter.com") ||
                    host == "x.com" || host.endsWith(".x.com") || host == "t.co" ->
                SocialPlatform.TWITTER_X

            host == "tiktok.com" || host.endsWith(".tiktok.com") ||
                    host == "vm.tiktok.com" || host == "vt.tiktok.com" ->
                SocialPlatform.TIKTOK

            host == "youtube.com" || host.endsWith(".youtube.com") ||
                    host == "youtu.be" ->
                SocialPlatform.YOUTUBE

            host == "reddit.com" || host.endsWith(".reddit.com") ||
                    host == "redd.it" ->
                SocialPlatform.REDDIT

            else -> SocialPlatform.GENERIC_WEB
        }
    }

    fun isSupportedSocialUrl(rawUrl: String): Boolean {
        return detectPlatform(rawUrl) != SocialPlatform.GENERIC_WEB
    }

    fun getCandidatePackages(platform: SocialPlatform): List<String> {
        return when (platform) {
            SocialPlatform.INSTAGRAM -> listOf("com.instagram.android")
            SocialPlatform.FACEBOOK -> listOf("com.facebook.katana", "com.facebook.lite")
            SocialPlatform.TWITTER_X -> listOf("com.twitter.android", "com.twitter.android.lite")
            SocialPlatform.TIKTOK -> listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
            SocialPlatform.YOUTUBE -> listOf("com.google.android.youtube")
            SocialPlatform.REDDIT -> listOf("com.reddit.frontpage")
            SocialPlatform.GENERIC_WEB -> emptyList()
        }
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            pm.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves an intent specifically targeted at the native social media application
     * if installed and available.
     */
    fun resolveSocialAppIntent(context: Context, rawUrl: String): Intent? {
        val uri = parseUri(rawUrl) ?: return null
        val platform = detectPlatform(rawUrl)
        if (platform == SocialPlatform.GENERIC_WEB) return null

        val pm = context.packageManager
        val candidates = getCandidatePackages(platform)

        // 1. Try known platform candidate packages
        for (pkg in candidates) {
            if (isPackageInstalled(context, pkg)) {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val resolveInfo = pm.resolveActivity(intent, 0)
                if (resolveInfo != null) {
                    return intent
                }
            }
        }

        // 2. Check if any installed non-browser app can handle the deep link directly
        try {
            val genericIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val resolvedList = pm.queryIntentActivities(genericIntent, 0)
            val nonBrowserApp = resolvedList.firstOrNull { info ->
                val p = info.activityInfo.packageName.lowercase()
                !BROWSER_PACKAGE_NAMES.contains(p) && !p.contains("browser") && !p.contains("chrome")
            }
            if (nonBrowserApp != null) {
                return Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(nonBrowserApp.activityInfo.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        } catch (_: Exception) {
            // Fall through to null
        }

        return null
    }

    /**
     * Returns the human-readable destination name (e.g. "Instagram", "YouTube", "Browser").
     */
    fun getPreferredDestinationName(context: Context, rawUrl: String): String {
        val platform = detectPlatform(rawUrl)
        if (platform != SocialPlatform.GENERIC_WEB) {
            val hasApp = resolveSocialAppIntent(context, rawUrl) != null
            if (hasApp) {
                return platform.displayName
            }
        }
        return "Browser"
    }

    /**
     * Open link according to priority:
     * 1. Native platform application via deep link / intent.
     * 2. Browser fallback.
     */
    fun openSocialOrWebLink(
        context: Context,
        rawUrl: String,
        preferBrowserOnly: Boolean = false
    ): Boolean {
        val uri = parseUri(rawUrl)
        if (uri == null) {
            Toast.makeText(context, "Invalid link address", Toast.LENGTH_SHORT).show()
            return false
        }

        // Priority 1: Native Social Application
        if (!preferBrowserOnly) {
            val appIntent = resolveSocialAppIntent(context, rawUrl)
            if (appIntent != null) {
                try {
                    context.startActivity(appIntent)
                    return true
                } catch (_: ActivityNotFoundException) {
                    // Fall back to browser
                } catch (_: Exception) {
                    // Fall back to browser
                }
            }
        }

        // Priority 2: Standard Browser
        return try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No web browser found to open link", Toast.LENGTH_SHORT).show()
            false
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
