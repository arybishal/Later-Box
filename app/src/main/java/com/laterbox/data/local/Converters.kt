package com.laterbox.data.local

import androidx.room.TypeConverter
import com.laterbox.data.model.ContentType
import com.laterbox.data.model.DownloadStatus

class Converters {
    @TypeConverter
    fun fromContentType(value: ContentType): String = value.name

    @TypeConverter
    fun toContentType(value: String): ContentType = try {
        ContentType.valueOf(value)
    } catch (_: Exception) {
        ContentType.LINK
    }

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String = value.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = try {
        DownloadStatus.valueOf(value)
    } catch (_: Exception) {
        DownloadStatus.NONE
    }
}
