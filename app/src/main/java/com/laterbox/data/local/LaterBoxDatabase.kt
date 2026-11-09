package com.laterbox.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.laterbox.data.local.dao.PlaylistDao
import com.laterbox.data.local.dao.SavedItemDao
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity

@Database(
    entities = [
        SavedItemEntity::class,
        PlaylistEntity::class,
        ItemPlaylistCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LaterBoxDatabase : RoomDatabase() {

    abstract fun savedItemDao(): SavedItemDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: LaterBoxDatabase? = null

        fun getInstance(context: Context): LaterBoxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LaterBoxDatabase::class.java,
                    "later_box.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val now = System.currentTimeMillis()
                            db.execSQL(
                                """
                                INSERT INTO playlists (name, description, iconName, coverUrl, createdAt, isDefaultInbox)
                                VALUES ('Inbox', 'Default destination for quick saves', 'inbox', NULL, $now, 1)
                                """.trimIndent()
                            )
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
