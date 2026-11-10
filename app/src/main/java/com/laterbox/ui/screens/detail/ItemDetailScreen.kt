package com.laterbox.ui.screens.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.laterbox.data.ai.security.SecureApiKeyStorage
import com.laterbox.data.local.entity.AiAnalysisEntity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.laterbox.LaterBoxApp
import com.laterbox.data.local.entity.SavedItemEntity
import com.laterbox.data.local.relation.PlaylistWithCount
import com.laterbox.data.model.ContentType
import com.laterbox.ui.components.AddTagDialog
import com.laterbox.ui.components.AddToPlaylistDialog
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.components.CreatePlaylistDialog
import com.laterbox.ui.components.EditNotesDialog
import com.laterbox.ui.theme.LaterBoxThemeTokens
import com.laterbox.util.SocialLinkResolver
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    onBack: () -> Unit,
    onOpenViewer: (SavedItemEntity) -> Unit,
    onNavigateToAiSettings: () -> Unit = {},
    viewModel: ItemDetailViewModel = viewModel(
        factory = ItemDetailViewModelFactory(itemId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val hasConsentedPrivacy by viewModel.hasConsentedPrivacy.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiActionError by viewModel.aiActionError.collectAsState()

    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAiPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteAiConfirm by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = extended.primaryBlack,
                strokeWidth = 2.dp,
                modifier = Modifier.size(36.dp)
            )
        }
        return
    }

    val item = uiState.item
    if (item == null || uiState.isNotFound) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Item not found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This saved item could not be loaded or has been deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onBack) {
                    Text("Go Back", color = extended.primaryBlack)
                }
            }
        }
        return
    }

    val preferredAppName = SocialLinkResolver.getPreferredDestinationName(context, item.url)
    val hasUrl = item.url.isNotBlank() && (item.url.startsWith("http://") || item.url.startsWith("https://"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Content type pill badge in header
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(extended.pillInactive)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.contentType.displayName.uppercase(Locale.ROOT),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extended.primaryBlack,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = extended.primaryBlack
                        )
                    }
                },
                actions = {
                    // Favorite Toggle Action
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (item.isFavorite) "Favorited" else "Favorite",
                            tint = if (item.isFavorite) extended.primaryBlack else extended.textSecondary
                        )
                    }

                    // More Actions Overflow
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = extended.primaryBlack
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add to Playlist") },
                                onClick = {
                                    showMenu = false
                                    showPlaylistDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = extended.primaryBlack)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (item.notes.isNullOrBlank()) "Add Note" else "Edit Note") },
                                onClick = {
                                    showMenu = false
                                    showEditNotesDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = extended.primaryBlack)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Add Tag") },
                                onClick = {
                                    showMenu = false
                                    showAddTagDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = extended.primaryBlack)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (item.isArchived) "Unarchive" else "Archive") },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleArchive()
                                },
                                leadingIcon = {
                                    Icon(
                                        if (item.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                        contentDescription = null,
                                        tint = extended.primaryBlack
                                    )
                                }
                            )

                            if (hasUrl) {
                                DropdownMenuItem(
                                    text = { Text("Copy Link") },
                                    onClick = {
                                        showMenu = false
                                        copyToClipboard(context, item.url)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = extended.primaryBlack)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    onClick = {
                                        showMenu = false
                                        shareItem(context, item)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = extended.primaryBlack)
                                    }
                                )
                            }

                            HorizontalDivider(color = extended.border)

                            DropdownMenuItem(
                                text = { Text("Move to Trash", color = extended.warmError) },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirmDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = extended.warmError)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // =========================================================================
            // 1. CONTENT PREVIEW (Centerpiece)
            // =========================================================================
            ContentPreviewSection(
                item = item,
                onOpenViewer = { onOpenViewer(item) },
                onOpenOriginal = {
                    if (hasUrl) {
                        viewModel.recordLastOpened()
                        SocialLinkResolver.openSocialOrWebLink(context, item.url)
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // =========================================================================
            // 2. ITEM TITLE & SOURCE METADATA
            // =========================================================================
            Text(
                text = item.title.ifBlank { "Untitled Save" },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                ),
                color = extended.primaryBlack
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row: Domain/Source + Saved Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Source Icon Circle
                val initialChar = (item.sourceDomain?.take(1) ?: item.sourceApp?.take(1) ?: "W").uppercase(Locale.ROOT)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(extended.pillInactive),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialChar,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = extended.primaryBlack
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = item.sourceDomain ?: item.sourceApp ?: "Saved note",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = extended.primaryBlack
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary
                )

                Text(
                    text = formatSavedDate(item.savedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary
                )
            }

            // Compact Original URL Row (if available)
            if (hasUrl) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(extended.pillInactive)
                        .clickable { copyToClipboard(context, item.url) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = extended.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.url,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = extended.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = extended.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(18.dp))

            // =========================================================================
            // 3. ABOUT THIS ITEM (Extracted metadata)
            // =========================================================================
            Text(
                text = "About this item",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = extended.primaryBlack
            )

            Spacer(modifier = Modifier.height(8.dp))

            val descriptionText = item.description?.trim()
            if (!descriptionText.isNullOrBlank()) {
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    ),
                    color = extended.textSecondary
                )
            } else {
                Text(
                    text = "No description provided by source.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = extended.textSecondary
                )
            }

            // Phase 2 AI Content Intelligence Section
            Phase2AiExtensionSlot(
                item = item,
                aiAnalysis = aiAnalysis,
                isAiEnabled = isAiEnabled,
                isAiLoading = isAiLoading,
                aiActionError = aiActionError,
                onAnalyze = {
                    if (!hasConsentedPrivacy) {
                        showAiPrivacyDialog = true
                    } else {
                        viewModel.analyzeWithAi(forceRegenerate = false)
                    }
                },
                onRegenerate = {
                    if (!hasConsentedPrivacy) {
                        showAiPrivacyDialog = true
                    } else {
                        viewModel.analyzeWithAi(forceRegenerate = true)
                    }
                },
                onDeleteAnalysis = {
                    showDeleteAiConfirm = true
                },
                onNavigateToSettings = onNavigateToAiSettings
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 4. PRIMARY ACTIONS: OPEN ORIGINAL LINK & IN-APP PREVIEW
            // =========================================================================
            if (hasUrl) {
                // Prominent Primary Action: Open in Native App or Browser
                val actionLabel = if (preferredAppName != "Browser") "Open in $preferredAppName" else "Open Original Link"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(extended.softYellow)
                        .clickable {
                            viewModel.recordLastOpened()
                            SocialLinkResolver.openSocialOrWebLink(context, item.url)
                        }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF111111)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary In-App Preview / Read Button
                val previewLabel = when (item.contentType) {
                    ContentType.VIDEO -> "Watch Video in Later Box"
                    ContentType.IMAGE -> "View Fullscreen Image"
                    ContentType.ARTICLE, ContentType.LINK -> "Read in Later Box Web Viewer"
                    ContentType.DOCUMENT -> "Preview Document"
                    ContentType.NOTE -> "Open Note Viewer"
                    ContentType.AUDIO -> "Play Audio in Later Box"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(extended.pillInactive)
                        .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                        .clickable { onOpenViewer(item) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = extended.primaryBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = previewLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = extended.primaryBlack
                        )
                    }
                }
            } else if (item.contentType == ContentType.NOTE) {
                // If it's a personal note without a web URL
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(extended.softYellow)
                        .clickable { showEditNotesDialog = true }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Note Content",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF111111)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // =========================================================================
            // 5. ORGANIZATION: PLAYLISTS, TAGS, AND PERSONAL NOTES
            // =========================================================================
            // Playlists Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playlists",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = extended.primaryBlack
                )

                Text(
                    text = "+ Add to Playlist",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = extended.primaryBlack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showPlaylistDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.playlists.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.playlists.forEach { playlist ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(extended.pillInactive)
                                .border(1.dp, extended.border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = extended.primaryBlack,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = extended.primaryBlack
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Not assigned to any playlist yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tags Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = extended.primaryBlack
                )

                Text(
                    text = "+ Add Tag",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = extended.primaryBlack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showAddTagDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.tags.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.tags.forEach { tag ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(extended.pillInactive)
                                .border(1.dp, extended.border, RoundedCornerShape(8.dp))
                                .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = extended.primaryBlack
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag",
                                tint = extended.textSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .clickable { viewModel.removeTag(tag.id) }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No tags added yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Personal Notes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Notes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = extended.primaryBlack
                )

                Text(
                    text = if (item.notes.isNullOrBlank()) "+ Add Note" else "Edit Note",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = extended.primaryBlack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showEditNotesDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!item.notes.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(extended.pillInactive)
                        .border(1.dp, extended.border, RoundedCornerShape(10.dp))
                        .clickable { showEditNotesDialog = true }
                        .padding(14.dp)
                ) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        ),
                        color = extended.primaryBlack
                    )
                }
            } else {
                Text(
                    text = "You haven't added any notes to this item yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 6. DESTRUCTIVE / TRASH ACTION (Zero Red Font compliant)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteConfirmDialog = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = extended.warmError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Move this item to Trash",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = extended.warmError
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================
    if (showPlaylistDialog) {
        val selectedIds = uiState.playlists.map { it.id }
        val allPlaylistEntities = uiState.allPlaylists.map { it.toPlaylistEntity() }

        AddToPlaylistDialog(
            allPlaylists = allPlaylistEntities,
            initiallySelectedIds = selectedIds,
            onDismiss = { showPlaylistDialog = false },
            onSavePlaylists = { newIds ->
                viewModel.updatePlaylists(newIds)
                showPlaylistDialog = false
            },
            onCreateNewPlaylist = {
                showPlaylistDialog = false
                showCreatePlaylistDialog = true
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name, description, iconName ->
                scope.launch {
                    val newId = LaterBoxApp.instance.playlistRepository.createPlaylist(name, description, iconName)
                    viewModel.updatePlaylists(uiState.playlists.map { it.id } + newId)
                    showCreatePlaylistDialog = false
                }
            }
        )
    }

    if (showEditNotesDialog) {
        EditNotesDialog(
            initialNotes = item.notes,
            onDismiss = { showEditNotesDialog = false },
            onSave = { updatedNotes ->
                viewModel.updateNotes(updatedNotes)
                showEditNotesDialog = false
            }
        )
    }

    if (showAddTagDialog) {
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onConfirm = { tagName ->
                viewModel.addTag(tagName)
                showAddTagDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmationDialog(
            title = "Move to Trash?",
            message = "This item will be moved to Trash. You can restore it anytime from Settings → Trash.",
            confirmLabel = "Move to Trash",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirmDialog = false
                viewModel.moveToTrash(onCompleted = onBack)
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    if (showAiPrivacyDialog) {
        ConfirmationDialog(
            title = "Gemini AI Content Analysis",
            message = "Later Box uses Google Gemini API to analyze saved content (title, notes, web metadata or image) and generate structured insights.\n\nContent is sent to Google using your configured API key. Your data is not stored on external Later Box servers. Proceed with analysis?",
            confirmLabel = "Agree & Analyze",
            isDestructive = false,
            onConfirm = {
                viewModel.grantPrivacyConsent()
                showAiPrivacyDialog = false
                viewModel.analyzeWithAi(forceRegenerate = false)
            },
            onDismiss = { showAiPrivacyDialog = false }
        )
    }

    if (showDeleteAiConfirm) {
        ConfirmationDialog(
            title = "Delete AI Insights?",
            message = "This will remove the generated AI summary, takeaways, and tags for this item. You can regenerate them at any time.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteAiConfirm = false
                viewModel.deleteAiAnalysis()
            },
            onDismiss = { showDeleteAiConfirm = false }
        )
    }
}

/**
 * Visual Centerpiece Content Preview adaptable across all supported content types:
 * Images, Videos, Articles/Links, Documents, and Notes.
 */
@Composable
private fun ContentPreviewSection(
    item: SavedItemEntity,
    onOpenViewer: () -> Unit,
    onOpenOriginal: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val previewModel = item.localMediaPath?.let { File(it) } ?: item.thumbnailUrl ?: item.url

    when (item.contentType) {
        ContentType.IMAGE -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 340.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(extended.pillInactive)
                    .border(1.dp, extended.border, RoundedCornerShape(16.dp))
                    .clickable { onOpenViewer() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(previewModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Enlarge Hint Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        ContentType.VIDEO -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(extended.pillInactive)
                    .border(1.dp, extended.border, RoundedCornerShape(16.dp))
                    .clickable { onOpenViewer() },
                contentAlignment = Alignment.Center
            ) {
                if (!item.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dark Scrim Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                // Circular Play Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(extended.softYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Video Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VIDEO PREVIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        ContentType.ARTICLE, ContentType.LINK -> {
            if (!item.thumbnailUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(extended.pillInactive)
                        .border(1.dp, extended.border, RoundedCornerShape(16.dp))
                        .clickable { onOpenViewer() },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Web Reader Preview Hint
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Preview",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // Editorial Fallback Card when no thumbnail exists
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(extended.pillInactive)
                        .border(1.dp, extended.border, RoundedCornerShape(16.dp))
                        .clickable { onOpenOriginal() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = extended.textSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.sourceDomain ?: "Web Link",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = extended.primaryBlack
                        )
                    }
                }
            }
        }

        ContentType.DOCUMENT -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(extended.pillInactive)
                    .border(1.dp, extended.border, RoundedCornerShape(16.dp))
                    .clickable { onOpenViewer() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(extended.softYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title.ifBlank { "Document File" },
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = extended.primaryBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.mimeType ?: "Document • Tap to preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }
            }
        }

        ContentType.NOTE -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(extended.pillInactive)
                    .border(1.dp, extended.border, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = extended.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SAVED NOTE CONTENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.notes?.ifBlank { item.title } ?: item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        color = extended.primaryBlack
                    )
                }
            }
        }

        else -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(extended.pillInactive)
                    .border(1.dp, extended.border, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Saved Content Preview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extended.textSecondary
                )
            }
        }
    }
}

/**
 * Phase 2 AI Content Intelligence Extension Point.
 * Displays structured insights powered by Google Gemini API.
 * Strict adherence to the approved color palette:
 * - Background: Clean card surface (White in light, dark surface in dark)
 * - Accent: Soft Butter-Yellow (#FFF4B8 / softYellow)
 * - Typography: Black (#111111 / primaryBlack) / Charcoal secondary
 * - Absolutely ZERO #9E6F21 and ZERO red fonts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase2AiExtensionSlot(
    item: SavedItemEntity,
    aiAnalysis: AiAnalysisEntity?,
    isAiEnabled: Boolean,
    isAiLoading: Boolean,
    aiActionError: String?,
    onAnalyze: () -> Unit,
    onRegenerate: () -> Unit,
    onDeleteAnalysis: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val hasApiKey = remember(context) { SecureApiKeyStorage.getInstance(context).hasApiKey() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(extended.cardBackground)
            .border(1.dp, extended.border, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        when {
            // State 1: Global AI Disabled
            !isAiEnabled -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(extended.pillInactive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = extended.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Content Intelligence",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = extended.primaryBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Generate instant summaries, smart categories, and takeaways with Gemini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, extended.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Enable Gemini in Settings",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = extended.primaryBlack,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // State 2: AI Enabled but No API Key Configured
            !hasApiKey -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(extended.pillInactive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = extended.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemini API Key Required",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = extended.primaryBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Add your Google Gemini API key to activate AI analysis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, extended.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Set API Key in Settings",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = extended.primaryBlack,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // State 3: Analysis in Progress / Loading
            isAiLoading || aiAnalysis?.status == "PENDING" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Analyzing with Gemini AI…",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = extended.primaryBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Extracting summary, takeaways, and entities…",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }
            }

            // State 4: Analysis Failed / Error
            aiActionError != null || aiAnalysis?.status == "FAILED" -> {
                val errorMsg = aiActionError ?: aiAnalysis?.errorMessage ?: "Analysis failed. Please check network or API quota."
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(extended.pillInactive),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = extended.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Analysis Unavailable",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = extended.primaryBlack
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAnalyze,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extended.primaryBlack,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Retry Analysis",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // State 5: Not Analyzed Yet
            aiAnalysis == null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(extended.softYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = extended.primaryBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemini AI Insights",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = extended.primaryBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Generate a structured summary, key takeaways, and category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAnalyze,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extended.softYellow,
                        contentColor = extended.primaryBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate AI Insights",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = extended.primaryBlack
                    )
                }
            }

            // State 6: Completed AI Analysis (SUCCESS)
            else -> {
                // Header with Sparkle, Model Badge, and Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(extended.softYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = extended.primaryBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "AI Insights",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = extended.primaryBlack
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(extended.softYellowSubtle)
                                .border(1.dp, extended.softYellowBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = aiAnalysis.modelUsed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extended.primaryBlack
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = extended.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteAnalysis,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Analysis",
                                tint = extended.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Badges: Category, Location, Language
                val badges = buildList {
                    aiAnalysis.category?.takeIf { it.isNotBlank() }?.let { add("📁 $it") }
                    aiAnalysis.locationName?.takeIf { it.isNotBlank() }?.let { add("📍 $it") }
                    aiAnalysis.language?.takeIf { it.isNotBlank() && it != "English" }?.let { add("🌐 $it") }
                }

                if (badges.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        badges.forEach { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(extended.pillInactive)
                                    .border(1.dp, extended.border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = extended.primaryBlack
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Short Description
                aiAnalysis.shortDescription?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // What It's About (if distinct and present)
                aiAnalysis.whatItsAbout?.takeIf { it.isNotBlank() && it != aiAnalysis.shortDescription }?.let { about ->
                    Text(
                        text = "What it's about",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = about,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        ),
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Key Takeaways
                val takeaways = aiAnalysis.getKeyTakeaways()
                if (takeaways.isNotEmpty()) {
                    Text(
                        text = "Key Takeaways",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    takeaways.forEach { takeaway ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = extended.primaryBlack,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Text(
                                text = takeaway,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                color = extended.primaryBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Entities and People
                val entities = aiAnalysis.getPeopleAndEntities()
                if (entities.isNotEmpty()) {
                    Text(
                        text = "Entities & Mentions",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        entities.forEach { entity ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(extended.pillInactive)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = entity,
                                    fontSize = 11.sp,
                                    color = extended.primaryBlack
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Footer timestamp
                HorizontalDivider(color = extended.border)
                Spacer(modifier = Modifier.height(8.dp))
                val dateStr = remember(aiAnalysis.analyzedAt) {
                    SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(aiAnalysis.analyzedAt))
                }
                Text(
                    text = "Analyzed on $dateStr",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = extended.textSecondary
                )
            }
        }
    }
}

private fun formatSavedDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000L}m ago"
        diff < 86400_000L -> "${diff / 3600_000L}h ago"
        diff < 604800_000L -> "${diff / 86400_000L}d ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Later Box Link", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareItem(context: Context, item: SavedItemEntity) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.url}")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share"))
}
