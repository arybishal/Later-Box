package com.laterbox.data.repository

import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoritesCollectionTest {

    private val items = mutableMapOf<Long, SavedItemEntity>()

    @Before
    fun setUp() {
        items.clear()
        for (i in 1..10) {
            val id = i.toLong()
            items[id] = SavedItemEntity(
                id = id,
                title = "Item $i",
                url = "https://example.com/$i",
                contentType = ContentType.ARTICLE,
                isFavorite = (i <= 3), // items 1, 2, 3 are initially favorites
                isDeleted = false
            )
        }
    }

    private fun getActiveFavorites(): List<SavedItemEntity> {
        return items.values.filter { !it.isDeleted && it.isFavorite }
    }

    private fun toggleFavorite(id: Long) {
        val current = items[id] ?: return
        items[id] = current.copy(isFavorite = !current.isFavorite)
    }

    private fun softDelete(id: Long) {
        val current = items[id] ?: return
        items[id] = current.copy(isDeleted = true, deletedAt = System.currentTimeMillis())
    }

    private fun restoreFromTrash(id: Long) {
        val current = items[id] ?: return
        items[id] = current.copy(isDeleted = false, deletedAt = null)
    }

    @Test
    fun testInitialFavoritesCount() {
        val favs = getActiveFavorites()
        assertEquals(3, favs.size)
        assertTrue(favs.all { it.isFavorite && !it.isDeleted })
    }

    @Test
    fun testAddToFavorites() {
        val targetId = 5L
        assertFalse(items[targetId]!!.isFavorite)

        toggleFavorite(targetId)

        assertTrue(items[targetId]!!.isFavorite)
        val favs = getActiveFavorites()
        assertEquals(4, favs.size)
        assertTrue(favs.any { it.id == targetId })
    }

    @Test
    fun testRemoveFromFavoritesDoesNotDeleteItem() {
        val targetId = 1L
        assertTrue(items[targetId]!!.isFavorite)

        // User unhearts the item
        toggleFavorite(targetId)

        assertFalse(items[targetId]!!.isFavorite)
        assertFalse(items[targetId]!!.isDeleted) // Item is NOT deleted
        val favs = getActiveFavorites()
        assertEquals(2, favs.size)
        assertFalse(favs.any { it.id == targetId })

        // Item still safely exists in active library
        val activeItems = items.values.filter { !it.isDeleted }
        assertEquals(10, activeItems.size)
    }

    @Test
    fun testDeletingItemRemovesItFromFavoritesView() {
        val targetId = 2L
        assertTrue(items[targetId]!!.isFavorite)

        // Item is moved to Trash
        softDelete(targetId)

        assertTrue(items[targetId]!!.isDeleted)
        // Active favorites no longer includes the trashed item
        val favs = getActiveFavorites()
        assertEquals(2, favs.size)
        assertFalse(favs.any { it.id == targetId })
    }

    @Test
    fun testRestoringFromTrashPreservesFavoriteState() {
        val targetId = 3L
        assertTrue(items[targetId]!!.isFavorite)

        // Trash the item
        softDelete(targetId)
        assertEquals(2, getActiveFavorites().size)

        // Restore from Trash
        restoreFromTrash(targetId)

        assertFalse(items[targetId]!!.isDeleted)
        assertTrue(items[targetId]!!.isFavorite) // Preserved
        val favs = getActiveFavorites()
        assertEquals(3, favs.size)
        assertTrue(favs.any { it.id == targetId })
    }

    @Test
    fun testPlaylistDeletionCascadesToFavorites() {
        // Items 1, 2 belong to playlist A
        val playlistItemIds = listOf(1L, 2L)

        // Deleting playlist moves all its items to Trash
        for (id in playlistItemIds) {
            softDelete(id)
        }

        // Only Item 3 remains in active Favorites
        val favs = getActiveFavorites()
        assertEquals(1, favs.size)
        assertEquals(3L, favs[0].id)
    }
}
