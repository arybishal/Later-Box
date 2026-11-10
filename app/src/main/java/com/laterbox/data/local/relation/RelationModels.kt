package com.laterbox.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.laterbox.data.local.entity.ItemPlaylistCrossRef
import com.laterbox.data.local.entity.PlaylistEntity
import com.laterbox.data.local.entity.SavedItemEntity

data class SavedItemWithPlaylists(
    @Embedded
    val item: SavedItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemPlaylistCrossRef::class,
            parentColumn = "contentId",
            entityColumn = "playlistId"
        )
    )
    val playlists: List<PlaylistEntity> = emptyList()
)

data class PlaylistWithItems(
    @Embedded
    val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemPlaylistCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "contentId"
        )
    )
    val items: List<SavedItemEntity> = emptyList()
)

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val description: String?,
    val iconName: String?,
    val coverUrl: String?,
    val createdAt: Long,
    val isDefaultInbox: Boolean,
    val itemCount: Int
) {
    fun toPlaylistEntity(): PlaylistEntity = PlaylistEntity(
        id = id,
        name = name,
        description = description,
        iconName = iconName,
        coverUrl = coverUrl,
        createdAt = createdAt,
        isDefaultInbox = isDefaultInbox
    )
}
