package com.laterbox.ui.screens.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import com.laterbox.data.local.entity.ReminderEntity
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.PlaylistWithCount
import com.laterbox.data.model.ContentType
import com.laterbox.ui.components.formatRelativeDate
import com.laterbox.ui.theme.LaterBoxThemeTokens
import com.laterbox.ui.theme.SerifHeadlineFontFamily
import java.io.File

/**
 * 3.1 Header: Spacious, modern header with uppercase category status tag,
 * prominent "Later Box" editorial headline, dynamic live saved items count,
 * circular search action, and circular settings/menu action.
 */
@Composable
fun HomeHeader(
    totalCount: Int,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SAVED MEMORIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = extended.secondaryText,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Later Box",
                fontFamily = SerifHeadlineFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = extended.primaryText,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$totalCount items saved",
                fontSize = 13.sp,
                color = extended.secondaryText
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Search Action
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(1.dp, CircleShape)
                    .clip(CircleShape)
                    .background(extended.cardBackground)
                    .border(1.dp, extended.border, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToSearch
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = extended.primaryIcons,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Circular Settings Action
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(1.dp, CircleShape)
                    .clip(CircleShape)
                    .background(extended.cardBackground)
                    .border(1.dp, extended.border, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToSettings
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = extended.primaryIcons,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 3.2 Welcome and Activity Area: Displays live activity metrics from Room database.
 * No fabricated statistics or fake activity.
 */
@Composable
fun HomeActivitySummaryCard(
    totalCount: Int,
    favoriteCount: Int,
    collectionsCount: Int,
    latestItem: SavedItemEntity?,
    onOpenLatest: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(26.dp), spotColor = Color(0x12000000))
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, extended.border, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = extended.interactiveAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Overview",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.primaryText
                    )
                }

                if (latestItem != null) {
                    Text(
                        text = "Updated ${formatRelativeDate(latestItem.savedAt)}",
                        fontSize = 11.sp,
                        color = extended.secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Three dynamic metric columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityMetricColumn(
                    count = totalCount,
                    label = "Saved Items",
                    primaryColor = extended.primaryText,
                    secondaryColor = extended.secondaryText
                )

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(extended.border)
                )

                ActivityMetricColumn(
                    count = favoriteCount,
                    label = "Favorites",
                    primaryColor = extended.primaryText,
                    secondaryColor = extended.secondaryText
                )

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(extended.border)
                )

                ActivityMetricColumn(
                    count = collectionsCount,
                    label = "Collections",
                    primaryColor = extended.primaryText,
                    secondaryColor = extended.secondaryText
                )
            }

            if (latestItem != null && onOpenLatest != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(extended.surfaceSecondary)
                        .clickable(onClick = onOpenLatest)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent: \"${latestItem.title}\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = extended.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extended.interactiveAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityMetricColumn(
    count: Int,
    label: String,
    primaryColor: Color,
    secondaryColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = secondaryColor
        )
    }
}

/**
 * Upcoming Events Preview Card for Home screen.
 */
@Composable
fun HomeUpcomingEventsCard(
    upcomingReminders: List<ReminderEntity>,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (upcomingReminders.isEmpty()) return

    val extended = LaterBoxThemeTokens.extended
    val firstEvent = upcomingReminders.first()
    val eventIcon = when (firstEvent.eventType) {
        "BIRTHDAY" -> Icons.Default.Cake
        "ANNIVERSARY" -> Icons.Default.Favorite
        "EVENT" -> Icons.Default.Event
        else -> Icons.Default.Notifications
    }

    val emojiLabel = when (firstEvent.eventType) {
        "BIRTHDAY" -> "Birthday"
        "ANNIVERSARY" -> "Anniversary"
        "EVENT" -> "Event"
        else -> "Reminder"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onNavigateToEvents),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, extended.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(extended.surfaceSecondary)
                        .border(1.dp, extended.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = eventIcon,
                        contentDescription = null,
                        tint = extended.primaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = emojiLabel.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.interactiveAccent,
                            letterSpacing = 0.5.sp
                        )
                        if (upcomingReminders.size > 1) {
                            Text(
                                text = " · +${upcomingReminders.size - 1} more",
                                fontSize = 10.sp,
                                color = extended.tertiaryText
                            )
                        }
                    }
                    Text(
                        text = firstEvent.eventTitle ?: "Upcoming Reminder",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = extended.primaryBlack,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = "View →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extended.primaryText
            )
        }
    }
}

/**
 * 3.3 Collections Section: Horizontal scrolling collections carousel.
 * White rounded 24dp cards, live counts, icons, subtle elevation.
 */
@Composable
fun HomeCollectionsSection(
    totalCount: Int,
    favoriteCount: Int,
    playlists: List<PlaylistWithCount>,
    onNavigateToLibrary: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Collections",
                fontFamily = SerifHeadlineFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = extended.primaryText
            )

            Text(
                text = "+ New",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = extended.interactiveAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCreatePlaylist
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            // "All" Collection Card
            item {
                CollectionCard(
                    title = "All Items",
                    count = totalCount,
                    icon = Icons.Default.BookmarkBorder,
                    onClick = onNavigateToLibrary
                )
            }

            // "Favorites" Collection Card
            item {
                CollectionCard(
                    title = "Favorites",
                    count = favoriteCount,
                    icon = Icons.Default.Favorite,
                    onClick = onNavigateToFavorites
                )
            }

            // User Playlists / Collections
            items(playlists, key = { it.id }) { playlist ->
                CollectionCard(
                    title = playlist.name,
                    count = playlist.itemCount,
                    icon = Icons.Default.Folder,
                    onClick = { onNavigateToPlaylist(playlist.id) }
                )
            }

            // Quick Add Card
            item {
                AddCollectionCard(onClick = onCreatePlaylist)
            }
        }
    }
}

@Composable
fun CollectionCard(
    title: String,
    count: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Card(
        modifier = modifier
            .width(136.dp)
            .height(112.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, extended.border, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(extended.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = extended.primaryIcons,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extended.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$count items",
                    fontSize = 11.sp,
                    color = extended.secondaryText
                )
            }
        }
    }
}

@Composable
fun AddCollectionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Card(
        modifier = modifier
            .width(96.dp)
            .height(112.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, extended.border, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(extended.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New",
                    tint = extended.primaryIcons,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "New",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = extended.primaryText
            )
        }
    }
}

/**
 * 3.4 Memories Section Filter Chips:
 * Compact fully-rounded chips with real counts and neutral active states.
 */
@Composable
fun HomeFilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    totalCount: Int,
    favoriteCount: Int,
    articlesCount: Int,
    videosCount: Int,
    imagesCount: Int,
    notesCount: Int,
    snapCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    val filters = buildList {
        add("All" to totalCount)
        if (snapCount > 0) add("Snap ⚡" to snapCount)
        add("Favorites" to favoriteCount)
        add("Articles" to articlesCount)
        add("Videos" to videosCount)
        add("Images" to imagesCount)
        add("Notes" to notesCount)
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(filters) { (name, count) ->
            val isSelected = selectedFilter == name

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) extended.primaryText else extended.cardBackground)
                    .border(
                        1.dp,
                        if (isSelected) extended.primaryText else extended.border,
                        CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onFilterSelected(name) }
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$name $count",
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.background else extended.secondaryText
                )
            }
        }
    }
}

/**
 * 3.5 Saved Content Card:
 * Compact white rounded card (26dp radius) with soft shadow.
 * Left: Item title, short description / AI summary, source domain & saved date.
 * Right: Thumbnail / media preview with rounded corners (16dp).
 */
@Composable
fun MemorySavedContentCard(
    item: SavedItemEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val imageModel = item.localMediaPath?.let { File(it) } ?: item.thumbnailUrl
    var hasLoadError by remember(imageModel) { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, extended.border, RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT COLUMN: Title, summary, metadata
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Item Title (max 2 lines)
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extended.primaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                // Short description or AI summary
                val summary = item.description?.trim() ?: item.notes?.trim()
                if (!summary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary,
                        fontSize = 13.sp,
                        color = extended.secondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Metadata Row: Source initial, domain, relative date, favorite button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small source icon or initial
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (item.sourceDomain?.take(1) ?: item.title.take(1)).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.primaryText
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = item.sourceDomain ?: item.sourceApp ?: item.contentType.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = extended.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text(
                        text = " • ${formatRelativeDate(item.savedAt)}",
                        fontSize = 11.sp,
                        color = extended.tertiaryText
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (item.isFavorite) extended.interactiveAccent else extended.tertiaryText,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = extended.tertiaryText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // RIGHT: Media preview / thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(extended.surfaceSecondary)
                    .border(1.dp, extended.border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageModel != null && !hasLoadError) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .listener(onError = { _, _ -> hasLoadError = true })
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (item.contentType == ContentType.VIDEO) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = when (item.contentType) {
                            ContentType.VIDEO -> Icons.Default.PlayArrow
                            ContentType.ARTICLE -> Icons.Default.Description
                            ContentType.IMAGE -> Icons.Default.Image
                            ContentType.NOTE -> Icons.Default.Description
                            else -> Icons.Default.Link
                        },
                        contentDescription = null,
                        tint = extended.secondaryIcons,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

/**
 * 3.6 Floating AI Assistant Pill:
 * Pill-shaped elevated action ("Ask questions" with sparkle icon)
 * Floating above content with soft shadow, wired to Gemini AI.
 */
@Composable
fun FloatingAiAssistantPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(percent = 50), spotColor = Color(0x25000000))
            .clip(RoundedCornerShape(percent = 50))
            .background(extended.surfaceElevated)
            .border(1.dp, extended.border, RoundedCornerShape(percent = 50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Ask Gemini",
                tint = extended.interactiveAccent,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Ask questions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = extended.primaryText
            )
        }
    }
}

/**
 * Interactive Gemini Assistant Sheet:
 * Full AI experience accessible directly from the Floating AI Assistant pill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskGeminiAssistantSheet(
    sheetState: SheetState,
    isAiEnabled: Boolean,
    hasApiKey: Boolean,
    isAiQuerying: Boolean,
    aiResponse: String?,
    aiError: String?,
    onAskQuestion: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Gemini AI Assistant",
    subtitle: String = "Search & synthesize your saved memories",
    promptSuggestions: List<String> = listOf(
        "Summarize recent items",
        "What did I save about Android?",
        "Find my favorite articles"
    )
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var queryText by remember { mutableStateOf("") }

    val connectivityManager = remember {
        context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    }
    val isOnline = remember(connectivityManager) {
        val activeNet = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNet)
        caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = extended.cardBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(extended.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(extended.surfaceSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = extended.interactiveAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = extended.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = extended.secondaryIcons,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!isAiEnabled || !hasApiKey) {
                // Helpful activation card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = extended.surfaceSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (!hasApiKey) "Gemini API Key Required" else "Gemini AI is Disabled",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.primaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!hasApiKey)
                                "Configure your personal Google Gemini API key in Settings to analyze items and ask questions."
                            else
                                "Enable Gemini AI in Settings to activate intelligent summaries and questions.",
                            fontSize = 13.sp,
                            color = extended.secondaryText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onNavigateToSettings()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extended.primaryText,
                                contentColor = extended.cardBackground
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Open AI Settings", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                if (!isOnline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(extended.surfaceSecondary)
                            .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = extended.secondaryIcons,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Device is offline. Gemini AI requires internet to process queries.",
                                fontSize = 12.sp,
                                color = extended.secondaryText
                            )
                        }
                    }
                }

                // Suggestion chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    items(promptSuggestions) { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(extended.surfaceSecondary)
                                .border(1.dp, extended.border, CircleShape)
                                .clickable {
                                    queryText = prompt
                                    if (isOnline) {
                                        onAskQuestion(prompt)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                color = extended.primaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Question Input
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = {
                        Text(
                            "Ask anything about your saved content…",
                            fontSize = 13.sp,
                            color = extended.tertiaryText
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (queryText.isNotBlank() && isOnline) {
                                    onAskQuestion(queryText)
                                }
                            },
                            enabled = queryText.isNotBlank() && !isAiQuerying && isOnline
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (queryText.isNotBlank() && isOnline) extended.interactiveAccent else extended.disabled
                            )
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = extended.surfaceSecondary,
                        unfocusedContainerColor = extended.surfaceSecondary,
                        focusedBorderColor = extended.interactiveAccent,
                        unfocusedBorderColor = extended.border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Querying indicator
                if (isAiQuerying) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = extended.interactiveAccent,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini is analyzing your saved content…",
                            fontSize = 13.sp,
                            color = extended.secondaryText
                        )
                    }
                }

                // Error message
                if (aiError != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = aiError,
                        fontSize = 13.sp,
                        color = extended.warmError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(extended.surfaceSecondary)
                            .padding(12.dp)
                    )
                }

                // AI Response
                if (aiResponse != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = extended.surfaceSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Response",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extended.interactiveAccent
                                )

                                Text(
                                    text = "Copy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = extended.secondaryText,
                                    modifier = Modifier
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(aiResponse))
                                        }
                                        .padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = aiResponse,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = extended.primaryText
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean empty state when user hasn't saved anything yet.
 */
@Composable
fun HomeEmptyState(
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(26.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, extended.border, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = extended.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(extended.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = extended.primaryIcons,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Your saved world starts here",
                fontFamily = SerifHeadlineFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = extended.primaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Save links, articles, or videos from any app or browser. Later Box organizes them automatically for you.",
                fontSize = 13.sp,
                color = extended.secondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onSaveClick,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.primaryText,
                    contentColor = extended.cardBackground
                ),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = extended.cardBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save your first item",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.cardBackground
                    )
                }
            }
        }
    }
}
