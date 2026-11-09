package com.laterbox.data.repository

import com.laterbox.data.local.dao.TagDao
import com.laterbox.data.local.dao.TagWithCount
import com.laterbox.data.local.entity.ItemTagCrossRef
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

class TagRepository(
    private val tagDao: TagDao
) {

    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()

    fun getAllTagsWithCount(): Flow<List<TagWithCount>> = tagDao.getAllTagsWithCount()

    fun getTagsForItem(contentId: Long): Flow<List<TagEntity>> = tagDao.getTagsForItem(contentId)

    fun getItemsForTag(tagId: Long): Flow<List<SavedItemEntity>> = tagDao.getItemsForTag(tagId)

    suspend fun createOrGetTag(rawName: String): Long {
        val normalized = rawName.trim().lowercase()
        if (normalized.isEmpty()) return -1L

        val existing = tagDao.getTagByName(normalized)
        return existing?.id ?: tagDao.insertTag(TagEntity(name = normalized))
    }

    suspend fun renameTag(id: Long, newName: String) {
        val normalized = newName.trim().lowercase()
        if (normalized.isEmpty()) return

        val tag = tagDao.getTagById(id) ?: return
        tagDao.updateTag(tag.copy(name = normalized))
    }

    suspend fun deleteTag(id: Long) {
        tagDao.deleteTag(id)
    }

    suspend fun setTagsForItem(contentId: Long, tagNames: List<String>) {
        tagDao.setTagsForItem(contentId, tagNames)
    }

    suspend fun addTagToItem(contentId: Long, tagId: Long) {
        tagDao.insertCrossRef(ItemTagCrossRef(contentId = contentId, tagId = tagId))
    }

    suspend fun removeTagFromItem(contentId: Long, tagId: Long) {
        tagDao.deleteCrossRef(contentId, tagId)
    }
}
