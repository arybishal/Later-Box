package com.laterbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.data.model.SortOrder
import com.laterbox.data.model.ViewMode
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun LaterBoxHeader(
    greeting: String,
    subtitle: String? = null,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column {
                Text(
                    text = greeting,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = extended.textSecondary
                    )
                }
            }
        }

        actions()
    }
}

@Composable
fun GridListTogglePill(
    currentMode: ViewMode,
    onModeChanged: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, extended.border, RoundedCornerShape(12.dp))
            .background(extended.cardBackground)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isGrid = currentMode == ViewMode.GRID

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(if (isGrid) extended.gridListActive else Color.Transparent)
                .clickable { onModeChanged(ViewMode.GRID) }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Grid",
                fontSize = 13.sp,
                fontWeight = if (isGrid) FontWeight.Bold else FontWeight.Normal,
                color = if (isGrid) MaterialTheme.colorScheme.onBackground else extended.textSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(if (!isGrid) extended.gridListActive else Color.Transparent)
                .clickable { onModeChanged(ViewMode.LIST) }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "List",
                fontSize = 13.sp,
                fontWeight = if (!isGrid) FontWeight.Bold else FontWeight.Normal,
                color = if (!isGrid) MaterialTheme.colorScheme.onBackground else extended.textSecondary
            )
        }
    }
}

@Composable
fun SortDropdownSelector(
    currentSort: SortOrder,
    onSortChanged: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort",
            style = MaterialTheme.typography.bodyMedium,
            color = extended.textSecondary
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, extended.border, RoundedCornerShape(10.dp))
                    .background(extended.cardBackground)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentSort) {
                        SortOrder.NEWEST -> "Newest first"
                        SortOrder.OLDEST -> "Oldest first"
                        SortOrder.TITLE_AZ -> "Title (A-Z)"
                        SortOrder.LAST_OPENED -> "Recently opened"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = extended.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                SortOrder.values().forEach { sort ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = when (sort) {
                                    SortOrder.NEWEST -> "Newest first"
                                    SortOrder.OLDEST -> "Oldest first"
                                    SortOrder.TITLE_AZ -> "Title (A-Z)"
                                    SortOrder.LAST_OPENED -> "Recently opened"
                                },
                                fontWeight = if (sort == currentSort) FontWeight.Bold else FontWeight.Normal,
                                color = if (sort == currentSort) extended.amberAccent else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSortChanged(sort)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
