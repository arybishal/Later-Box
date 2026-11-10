package com.laterbox.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.ui.theme.LaterBoxThemeTokens
import com.laterbox.ui.theme.SerifHeadlineFontFamily

// =========================================================================================
// 1. CONTEXTUAL EMPTY STATES
// =========================================================================================

@Composable
fun ContextualEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon badge with subtle border
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(extended.cardBackground)
                .border(1.dp, extended.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = extended.primaryBlack,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = title,
            fontFamily = SerifHeadlineFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
            color = extended.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (primaryActionLabel != null && onPrimaryAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onPrimaryAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.primaryBlack,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = primaryActionLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        if (secondaryActionLabel != null && onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSecondaryAction) {
                Text(
                    text = secondaryActionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = extended.textSecondary
                )
            }
        }
    }
}

// =========================================================================================
// 2. ZERO SEARCH RESULTS STATE
// =========================================================================================

@Composable
fun ZeroSearchResultsState(
    query: String,
    hasActiveFilters: Boolean = false,
    onClearSearch: () -> Unit,
    onResetFilters: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(extended.cardBackground)
            .border(1.dp, extended.border, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(extended.pillInactive),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = extended.textSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "No matches found",
            fontFamily = SerifHeadlineFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (query.isNotBlank()) {
            Text(
                text = "We couldn't find any saved items matching \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "No items match your active filter selection.",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Helpful suggestions box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(extended.pillInactive)
                .padding(12.dp)
        ) {
            Text(
                text = "Search suggestions:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Check for typos or alternate spellings\n• Try searching for a website domain (e.g. youtube.com)\n• Use fewer, broader keywords",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = extended.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (query.isNotBlank()) {
                Button(
                    onClick = onClearSearch,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extended.primaryBlack,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Search", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            if (hasActiveFilters && onResetFilters != null) {
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Filters", fontSize = 13.sp, color = extended.primaryBlack)
                }
            }
        }
    }
}

// =========================================================================================
// 3. SKELETON PLACEHOLDERS & SHIMMER LOADING
// =========================================================================================

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    val shimmerColor = LaterBoxThemeTokens.extended.border.copy(alpha = alpha)
    return this.background(shimmerColor)
}

@Composable
fun SkeletonFeedCard(modifier: Modifier = Modifier) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(extended.cardBackground)
            .border(1.dp, extended.border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title placeholder line 1
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Title placeholder line 2
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Metadata chip placeholder
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun SkeletonGridCard(modifier: Modifier = Modifier) {
    val extended = LaterBoxThemeTokens.extended

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(extended.cardBackground)
            .border(1.dp, extended.border, RoundedCornerShape(12.dp))
    ) {
        // Thumbnail placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .shimmerEffect()
        )
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun SkeletonDetailView(modifier: Modifier = Modifier) {
    val extended = LaterBoxThemeTokens.extended

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Hero image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action row pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerEffect()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Body placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .shimmerEffect()
        )
    }
}

// =========================================================================================
// 4. ACTIONABLE ERROR STATES & DIAGNOSTICS
// =========================================================================================

@Composable
fun StateErrorView(
    title: String,
    message: String,
    technicalDetails: String? = null,
    onRetry: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    var isExpandedDetails by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(extended.cardBackground)
            .border(1.dp, extended.border, RoundedCornerShape(14.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(extended.pillInactive),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = extended.primaryBlack,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            fontFamily = SerifHeadlineFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
            color = extended.textSecondary,
            textAlign = TextAlign.Center
        )

        if (!technicalDetails.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isExpandedDetails = !isExpandedDetails }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpandedDetails) "Hide technical diagnostics" else "Show technical diagnostics",
                    style = MaterialTheme.typography.labelSmall,
                    color = extended.textSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpandedDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = extended.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpandedDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(extended.pillInactive)
                        .padding(10.dp)
                ) {
                    Text(
                        text = technicalDetails,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = extended.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extended.primaryBlack,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retry", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            if (secondaryActionLabel != null && onSecondaryAction != null) {
                OutlinedButton(
                    onClick = onSecondaryAction,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(secondaryActionLabel, fontSize = 13.sp, color = extended.primaryBlack)
                }
            }
        }
    }
}

// =========================================================================================
// 5. OFFLINE NOTICE BANNER
// =========================================================================================

@Composable
fun OfflineNoticeBanner(
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(extended.pillInactive)
            .border(1.dp, extended.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = extended.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Offline Mode",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Saved content is fully accessible offline. Online web preview and AI features will resume when reconnected.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = extended.textSecondary
            )
        }
        if (onDismiss != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Dismiss",
                tint = extended.textSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}

// =========================================================================================
// 6. SLOW NETWORK / MEASURABLE PROGRESS VIEW
// =========================================================================================

@Composable
fun SlowNetworkProgressView(
    stageDescription: String,
    progress: Float? = null,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(extended.cardBackground)
            .border(1.dp, extended.border, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stageDescription,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (progress != null && progress in 0f..1f) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = extended.primaryBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (progress != null && progress in 0f..1f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = extended.primaryText,
                trackColor = extended.pillInactive
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = extended.primaryText,
                trackColor = extended.pillInactive
            )
        }

        if (onCancel != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onCancel,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelSmall, color = extended.textSecondary)
                }
            }
        }
    }
}

// =========================================================================================
// 7. PERMISSION EXPLAINER CARD
// =========================================================================================

@Composable
fun PermissionNoticeCard(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.NotificationsActive,
    actionLabel: String = "Open Settings",
    onOpenSettings: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(extended.cardBackground)
            .border(1.dp, extended.border, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(extended.surfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = extended.primaryText,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = extended.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extended.primaryBlack,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(actionLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
                if (onDismiss != null) {
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Not now", fontSize = 12.sp, color = extended.textSecondary)
                    }
                }
            }
        }
    }
}

// =========================================================================================
// 8. INLINE FORM VALIDATION NOTICE
// =========================================================================================

@Composable
fun InlineValidationNotice(
    message: String,
    isError: Boolean = true,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val tint = if (isError) extended.warmError else extended.textSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.WarningAmber else Icons.Default.Info,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = tint
        )
    }
}
