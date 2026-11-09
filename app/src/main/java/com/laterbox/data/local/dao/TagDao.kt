package com.laterbox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.laterbox.data.local.entity.ItemTagCrossRef
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

data class TagWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val itemCount: Int
)

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getTagById(id: Long): TagEntity?

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: Long)

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("""
        SELECT t.id, t.name, t.createdAt, COUNT(s.id) AS itemCount
        FROM tags t
        LEFT JOIN item_tag_cross_ref c ON t.id = c.tagId
        LEFT JOIN saved_items s ON c.contentId = s.id AND s.isDeleted = 0
        GROUP BY t.id
        ORDER BY t.name COLLATE NOCASE ASC
    """)
    fun getAllTagsWithCount(): Flow<List<TagWithCount>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: ItemTagCrossRef)

    @Query("DELETE FROM item_tag_cross_ref WHERE contentId = :contentId AND tagId = :tagId")
    suspend fun deleteCrossRef(contentId: Long, tagId: Long)

    @Query("DELETE FROM item_tag_cross_ref WHERE contentId = :contentId")
    suspend fun deleteAllCrossRefsForContent(contentId: Long)

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN item_tag_cross_ref c ON t.id = c.tagId
        WHERE c.contentId = :contentId
        ORDER BY t.name COLLATE NOCASE ASC
    """)
    fun getTagsForItem(contentId: Long): Flow<List<TagEntity>>

    @Query("""
        SELECT s.* FROM saved_items s
        INNER JOIN item_tag_cross_ref c ON s.id = c.contentId
        WHERE c.tagId = :tagId AND s.isDeleted = 0
        ORDER BY s.savedAt DESC
    """)
    fun getItemsForTag(tagId: Long): Flow<List<SavedItemEntity>>

    @Transaction
    suspend fun setTagsForItem(contentId: Long, tagNames: List<String>) {
        deleteAllCrossRefsForContent(contentId)
        for (rawName in tagNames) {
            val trimmed = rawName.trim().lowercase()
            if (trimmed.isNotEmpty()) {
                var tag = getTagByName(trimmed)
                val tagId = if (tag != null) {
                    tag.id
                } else {
                    insertTag(TagEntity(name = trimmed))
                }
                insertCrossRef(ItemTagCrossRef(contentId = contentId, tagId = tagId))
            }
        }
    }

    @Query("SELECT * FROM tags")
    suspend fun getAllTagsSnapshot(): List<TagEntity>

    @Query("SELECT * FROM item_tag_cross_ref")
    suspend fun getAllTagCrossRefsSnapshot(): List<ItemTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<ItemTagCrossRef>)

    @Query("DELETE FROM tags")
    suspend fun clearAllTags()

    @Query("DELETE FROM item_tag_cross_ref")
    suspend fun clearAllTagCrossRefs()
}
