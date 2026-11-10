package com.laterbox.data.update

data class UpdateInfo(
    val tag: String,
    val versionName: String,
    val versionCode: Long,
    val name: String,
    val notes: String,
    val publishedAt: String,
    val releaseUrl: String,
    val apkUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val isNewer: Boolean,
    val expectedSha256: String? = null
) {
    val formattedSize: String
        get() {
            if (apkSizeBytes <= 0) return "Unknown size"
            val mb = apkSizeBytes.toDouble() / (1024 * 1024)
            return String.format(java.util.Locale.US, "%.1f MB", mb)
        }
}

/**
 * Robust semantic version parser and comparator.
 * Handles tags like "v1.3.1", "1.3.0", "v2.0.0-beta".
 * Does NOT do naive lexicographical string comparison.
 */
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val suffix: String? = null
) : Comparable<SemanticVersion> {

    override fun compareTo(other: SemanticVersion): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)
        // Versions without suffix are newer than versions with suffix (e.g. 1.0.0 > 1.0.0-rc1)
        if (suffix == null && other.suffix != null) return 1
        if (suffix != null && other.suffix == null) return -1
        return (suffix ?: "").compareTo(other.suffix ?: "")
    }

    companion object {
        fun parse(versionString: String): SemanticVersion? {
            val cleaned = versionString.trim().removePrefix("v").removePrefix("V")
            val parts = cleaned.split("-", limit = 2)
            val numbers = parts[0].split(".")
            val suffix = if (parts.size > 1) parts[1] else null

            val major = numbers.getOrNull(0)?.toIntOrNull() ?: return null
            val minor = numbers.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = numbers.getOrNull(2)?.toIntOrNull() ?: 0

            return SemanticVersion(major, minor, patch, suffix)
        }

        fun isVersionNewer(remoteTag: String, localVersionName: String): Boolean {
            val remote = parse(remoteTag) ?: return false
            val local = parse(localVersionName) ?: return false
            return remote > local
        }
    }
}
