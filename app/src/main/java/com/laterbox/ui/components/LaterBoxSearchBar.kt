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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.data.model.ContentType
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun LaterBoxSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search titles, notes, links",
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = extended.textSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = extended.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = extended.cardBackground,
            unfocusedContainerColor = extended.cardBackground,
            focusedBorderColor = extended.amberAccent,
            unfocusedBorderColor = extended.border,
            cursorColor = extended.amberAccent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PillFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val categories = listOf("All", "Favorites", "Inbox", "Videos", "Articles", "Images", "Documents", "Audio", "Notes")
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) extended.pillActive else extended.pillInactive)
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal scrollbar track matching exact screenshot styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "◀",
                fontSize = 8.sp,
                color = extended.textSecondary,
                modifier = Modifier.padding(end = 4.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(extended.border.copy(alpha = 0.5f))
            ) {
                val totalItems = categories.size.toFloat()
                val visibleIndex = listState.firstVisibleItemIndex.toFloat()
                val offsetRatio = (visibleIndex / (totalItems - 3f).coerceAtLeast(1f)).coerceIn(0f, 0.65f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(6.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = (offsetRatio * 180).dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(extended.textSecondary)
                )
            }

            Text(
                text = "▶",
                fontSize = 8.sp,
                color = extended.textSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
