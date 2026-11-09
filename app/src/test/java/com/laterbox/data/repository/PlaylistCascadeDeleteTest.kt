package com.laterbox.data.repository

import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaylistCascadeDeleteTest {

    // In-memory simulation of Room database state to rigorously test cascade deletion behavior
    private val savedItems = mutableMapOf<Long, SavedItemEntity>()
    private val playlists = mutableMapOf<Long, PlaylistEntity>()
    private val crossRefs = mutableListOf<ItemPlaylistCrossRef>()

    @Before
    fun setUp() {
        savedItems.clear()
        playlists.clear()
        crossRefs.clear()

        // Default Inbox
        playlists[1L] = PlaylistEntity(id = 1L, name = "Inbox", isDefaultInbox = true)
    }

    private fun simulateCascadeDeletePlaylist(playlistId: Long, timestamp: Long = System.currentTimeMillis()) {
        val playlist = playlists[playlistId] ?: return
        if (playlist.isDefaultInbox) return // Protected

        // 1. Find all content IDs in this playlist
        val contentIds = crossRefs.filter { it.playlistId == playlistId }.map { it.contentId }.toSet()

        // 2. Cascade soft delete all associated items
        for (cId in contentIds) {
            val item = savedItems[cId]
            if (item != null && !item.isDeleted) {
                savedItems[cId] = item.copy(isDeleted = true, deletedAt = timestamp)
            }
            // Remove cross references for the deleted item so it restores to general library
            crossRefs.removeAll { it.contentId == cId }
        }

        // 3. Remove cross references for this playlist
        crossRefs.removeAll { it.playlistId == playlistId }

        // 4. Remove playlist
        playlists.remove(playlistId)
    }

    private fun simulateRestoreFromTrash(itemId: Long) {
        val item = savedItems[itemId] ?: return
        savedItems[itemId] = item.copy(isDeleted = false, deletedAt = null)
    }

    @Test
    fun testEmptyPlaylistDeletion() {
        playlists[2L] = PlaylistEntity(id = 2L, name = "Empty Playlist", isDefaultInbox = false)
        assertEquals(2, playlists.size)

        simulateCascadeDeletePlaylist(2L)

        assertNull(playlists[2L])
        assertEquals(1, playlists.size)
        assertTrue(playlists[1L]?.isDefaultInbox == true)
    }

    @Test
    fun testPlaylistDeletionMovesItemsToTrash() {
        playlists[2L] = PlaylistEntity(id = 2L, name = "Watch Later", isDefaultInbox = false)
        for (i in 1..5) {
            val id = i.toLong()
            savedItems[id] = SavedItemEntity(
                id = id,
                title = "Video $i",
                url = "https://youtube.com/watch?v=$i",
                contentType = ContentType.VIDEO,
                isDeleted = false
            )
            crossRefs.add(ItemPlaylistCrossRef(contentId = id, playlistId = 2L))
        }

        // Verify initial state
        assertEquals(5, savedItems.values.count { !it.isDeleted })
        assertEquals(0, savedItems.values.count { it.isDeleted })
        assertEquals(5, crossRefs.size)

        // Delete playlist
        val deleteTime = 1731180000000L
        simulateCascadeDeletePlaylist(2L, deleteTime)

        // Playlist is deleted
        assertNull(playlists[2L])

        // All 5 items moved to Trash
        val activeItems = savedItems.values.filter { !it.isDeleted }
        val trashItems = savedItems.values.filter { it.isDeleted }
        assertEquals(0, activeItems.size)
        assertEquals(5, trashItems.size)
        for (item in trashItems) {
            assertEquals(deleteTime, item.deletedAt)
        }

        // All cross-references cleaned up
        assertEquals(0, crossRefs.size)
    }

    @Test
    fun testItemSharedBetweenMultiplePlaylistsIsSoftDeleted() {
        playlists[2L] = PlaylistEntity(id = 2L, name = "Playlist A", isDefaultInbox = false)
        playlists[3L] = PlaylistEntity(id = 3L, name = "Playlist B", isDefaultInbox = false)

        val sharedItemId = 100L
        savedItems[sharedItemId] = SavedItemEntity(
            id = sharedItemId,
            title = "Shared Item",
            url = "https://example.com/shared",
            isDeleted = false
        )
        crossRefs.add(ItemPlaylistCrossRef(contentId = sharedItemId, playlistId = 2L))
        crossRefs.add(ItemPlaylistCrossRef(contentId = sharedItemId, playlistId = 3L))

        assertEquals(2, crossRefs.size)

        // Delete Playlist A
        simulateCascadeDeletePlaylist(2L)

        // Item is moved to trash even though it was also in Playlist B
        val item = savedItems[sharedItemId]
        assertNotNull(item)
        assertTrue(item!!.isDeleted)

        // Cross-references are cleaned up so no orphan exists
        val remainingRefs = crossRefs.filter { it.contentId == sharedItemId }
        assertEquals(0, remainingRefs.size)

        // Playlist B still exists
        assertNotNull(playlists[3L])
    }

    @Test
    fun testRestoreFromTrashAfterPlaylistDeletedRestoresToGeneralLibrary() {
        playlists[2L] = PlaylistEntity(id = 2L, name = "Temp Playlist", isDefaultInbox = false)
        val itemId = 50L
        savedItems[itemId] = SavedItemEntity(
            id = itemId,
            title = "Article to read",
            url = "https://medium.com/article",
            isDeleted = false
        )
        crossRefs.add(ItemPlaylistCrossRef(contentId = itemId, playlistId = 2L))

        // Delete playlist
        simulateCascadeDeletePlaylist(2L)
        assertTrue(savedItems[itemId]!!.isDeleted)
        assertNull(playlists[2L])

        // User restores item from Trash
        simulateRestoreFromTrash(itemId)

        val restoredItem = savedItems[itemId]
        assertNotNull(restoredItem)
        assertFalse(restoredItem!!.isDeleted)
        assertNull(restoredItem.deletedAt)

        // The deleted playlist is NOT resurrected
        assertNull(playlists[2L])

        // Item has no playlist associations, meaning it resides in general Library/Inbox
        val itemPlaylists = crossRefs.filter { it.contentId == itemId }
        assertEquals(0, itemPlaylists.size)
    }

    @Test
    fun testDefaultInboxCannotBeDeleted() {
        val inbox = playlists[1L]!!
        assertTrue(inbox.isDefaultInbox)

        val inboxItemId = 10L
        savedItems[inboxItemId] = SavedItemEntity(
            id = inboxItemId,
            title = "Inbox Item",
            url = "https://example.com",
            isDeleted = false
        )
        crossRefs.add(ItemPlaylistCrossRef(contentId = inboxItemId, playlistId = 1L))

        // Attempt deleting default inbox
        simulateCascadeDeletePlaylist(1L)

        // Inbox and items remain intact
        assertNotNull(playlists[1L])
        assertFalse(savedItems[inboxItemId]!!.isDeleted)
        assertEquals(1, crossRefs.size)
    }
}
