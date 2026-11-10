package com.laterbox.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.laterbox.data.local.dao.AiAnalysisDao
import com.laterbox.data.local.dao.PlaylistDao
import com.laterbox.data.local.dao.ReminderDao
import com.laterbox.data.local.dao.SavedItemDao
import com.laterbox.data.local.dao.SearchHistoryDao
import com.laterbox.data.local.dao.TagDao
import com.laterbox.data.local.entity.AiAnalysisEntity
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.ItemTagCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.ReminderEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.entity.SearchHistoryEntity
import com.laterbox.data.local.entity.TagEntity

@Database(
    entities = [
        SavedItemEntity::class,
        PlaylistEntity::class,
        ItemPlaylistCrossRef::class,
        TagEntity::class,
        ItemTagCrossRef::class,
        ReminderEntity::class,
        SearchHistoryEntity::class,
        AiAnalysisEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LaterBoxDatabase : RoomDatabase() {

    abstract fun savedItemDao(): SavedItemDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun tagDao(): TagDao
    abstract fun reminderDao(): ReminderDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun aiAnalysisDao(): AiAnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: LaterBoxDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS item_tag_cross_ref (
                        contentId INTEGER NOT NULL,
                        tagId INTEGER NOT NULL,
                        addedAt INTEGER NOT NULL,
                        PRIMARY KEY(contentId, tagId),
                        FOREIGN KEY(contentId) REFERENCES saved_items(id) ON DELETE CASCADE,
                        FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_item_tag_cross_ref_contentId ON item_tag_cross_ref(contentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_item_tag_cross_ref_tagId ON item_tag_cross_ref(tagId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        contentId INTEGER NOT NULL,
                        dueAt INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        snoozedUntil INTEGER,
                        note TEXT,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(contentId) REFERENCES saved_items(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_contentId ON reminders(contentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_dueAt ON reminders(dueAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted ON reminders(isCompleted)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS search_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        query TEXT NOT NULL,
                        searchedAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_search_history_query ON search_history(query)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_analyses (
                        itemId INTEGER PRIMARY KEY NOT NULL,
                        shortDescription TEXT,
                        whatItsAbout TEXT,
                        category TEXT,
                        locationName TEXT,
                        locationAddress TEXT,
                        locationConfidence TEXT,
                        peopleAndEntitiesJson TEXT NOT NULL,
                        keyDetailsJson TEXT NOT NULL,
                        keyTakeawaysJson TEXT NOT NULL,
                        language TEXT,
                        modelUsed TEXT NOT NULL,
                        analyzedAt INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        contentFingerprint TEXT,
                        errorMessage TEXT,
                        FOREIGN KEY(itemId) REFERENCES saved_items(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_analyses_status ON ai_analyses(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_analyses_analyzedAt ON ai_analyses(analyzedAt)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // saved_items columns for Unified Memory Assistant
                db.execSQL("ALTER TABLE saved_items ADD COLUMN isSnap INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE saved_items ADD COLUMN aiTitle TEXT")
                db.execSQL("ALTER TABLE saved_items ADD COLUMN aiDescription TEXT")
                db.execSQL("ALTER TABLE saved_items ADD COLUMN captureMethod TEXT")
                db.execSQL("ALTER TABLE saved_items ADD COLUMN extractedText TEXT")
                db.execSQL("ALTER TABLE saved_items ADD COLUMN transcript TEXT")
                db.execSQL("ALTER TABLE saved_items ADD COLUMN sourcePackage TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_items_isSnap ON saved_items(isSnap)")

                // reminders columns for Events & Birthdays
                db.execSQL("ALTER TABLE reminders ADD COLUMN eventTitle TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN eventType TEXT NOT NULL DEFAULT 'REMINDER'")
                db.execSQL("ALTER TABLE reminders ADD COLUMN personName TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN recurrenceRule TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN notifyBeforeMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_eventType ON reminders(eventType)")

                // ai_analyses column for Semantic Search Embeddings
                db.execSQL("ALTER TABLE ai_analyses ADD COLUMN embeddingVectorJson TEXT")
            }
        }

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
