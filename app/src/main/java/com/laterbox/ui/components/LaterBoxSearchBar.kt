package com.laterbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.ui.components.glass.GlassSearchField
import com.laterbox.ui.components.glass.GlassSurface
import com.laterbox.ui.theme.GlassVariant
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun LaterBoxSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search titles, notes, links",
    modifier: Modifier = Modifier
) {
    GlassSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = placeholder,
        modifier = modifier
    )
}

@Composable
fun PillFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val categories = listOf("All", "Snap ⚡", "Favorites", "Inbox", "Videos", "Articles", "Images", "Documents", "Audio", "Notes")
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(extended.primaryText)
                            .border(1.dp, extended.primaryText, CircleShape)
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                } else {
                    GlassSurface(
                        variant = GlassVariant.SUBTLE,
                        shape = CircleShape,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onCategorySelected(category) }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = extended.secondaryText
                            )
                        }
                    }
                }
            }
        }
    }
}
