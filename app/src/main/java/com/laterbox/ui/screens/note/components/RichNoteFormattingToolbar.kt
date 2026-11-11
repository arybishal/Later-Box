package com.laterbox.ui.screens.note.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.data.model.richnote.ListType
import com.laterbox.data.model.richnote.SpanType
import com.laterbox.data.model.richnote.TextAlignment
import com.laterbox.data.model.richnote.TextStyleType
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun RichNoteFormattingToolbar(
    activeFormatting: Set<SpanType>,
    activeStyle: TextStyleType,
    activeAlignment: TextAlignment,
    activeListType: ListType,
    onToggleFormat: (SpanType) -> Unit,
    onSelectStyle: (TextStyleType) -> Unit,
    onSelectAlignment: (TextAlignment) -> Unit,
    onSelectListType: (ListType) -> Unit,
    onInsertImage: () -> Unit,
    onInsertLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = extended.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, extended.border),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Group 1: Heading / Text Hierarchy Styles
            StyleChip(
                label = "H1",
                isActive = activeStyle == TextStyleType.HEADING_1,
                onClick = {
                    onSelectStyle(if (activeStyle == TextStyleType.HEADING_1) TextStyleType.NORMAL else TextStyleType.HEADING_1)
                },
                contentDescription = "Heading 1"
            )

            StyleChip(
                label = "H2",
                isActive = activeStyle == TextStyleType.HEADING_2,
                onClick = {
                    onSelectStyle(if (activeStyle == TextStyleType.HEADING_2) TextStyleType.NORMAL else TextStyleType.HEADING_2)
                },
                contentDescription = "Heading 2"
            )

            StyleChip(
                label = "Title",
                isActive = activeStyle == TextStyleType.TITLE,
                onClick = {
                    onSelectStyle(if (activeStyle == TextStyleType.TITLE) TextStyleType.NORMAL else TextStyleType.TITLE)
                },
                contentDescription = "Document Title Style"
            )

            ToolbarDivider()

            // Group 2: Inline Formatting (Bold, Italic, Underline, Strikethrough)
            ToolbarIconButton(
                icon = Icons.Default.FormatBold,
                isActive = activeFormatting.contains(SpanType.BOLD),
                onClick = { onToggleFormat(SpanType.BOLD) },
                contentDescription = "Bold"
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatItalic,
                isActive = activeFormatting.contains(SpanType.ITALIC),
                onClick = { onToggleFormat(SpanType.ITALIC) },
                contentDescription = "Italic"
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatUnderlined,
                isActive = activeFormatting.contains(SpanType.UNDERLINE),
                onClick = { onToggleFormat(SpanType.UNDERLINE) },
                contentDescription = "Underline"
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatStrikethrough,
                isActive = activeFormatting.contains(SpanType.STRIKETHROUGH),
                onClick = { onToggleFormat(SpanType.STRIKETHROUGH) },
                contentDescription = "Strikethrough"
            )

            ToolbarDivider()

            // Group 3: Lists (Bullets, Numbers, Checklists)
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                isActive = activeListType == ListType.BULLET,
                onClick = { onSelectListType(ListType.BULLET) },
                contentDescription = "Bulleted List"
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatListNumbered,
                isActive = activeListType == ListType.NUMBERED,
                onClick = { onSelectListType(ListType.NUMBERED) },
                contentDescription = "Numbered List"
            )

            ToolbarIconButton(
                icon = Icons.Default.CheckBox,
                isActive = activeListType == ListType.CHECKLIST,
                onClick = { onSelectListType(ListType.CHECKLIST) },
                contentDescription = "Checklist"
            )

            ToolbarDivider()

            // Group 4: Alignment (Left, Center, Right)
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                isActive = activeAlignment == TextAlignment.LEFT,
                onClick = { onSelectAlignment(TextAlignment.LEFT) },
                contentDescription = "Align Left"
            )

            ToolbarIconButton(
                icon = Icons.Default.FormatAlignCenter,
                isActive = activeAlignment == TextAlignment.CENTER,
                onClick = { onSelectAlignment(TextAlignment.CENTER) },
                contentDescription = "Align Center"
            )

            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                isActive = activeAlignment == TextAlignment.RIGHT,
                onClick = { onSelectAlignment(TextAlignment.RIGHT) },
                contentDescription = "Align Right"
            )

            ToolbarDivider()

            // Group 5: Inserts (Image, Link)
            ToolbarIconButton(
                icon = Icons.Default.AddPhotoAlternate,
                isActive = false,
                onClick = onInsertImage,
                contentDescription = "Insert Image"
            )

            ToolbarIconButton(
                icon = Icons.Default.Link,
                isActive = activeFormatting.contains(SpanType.LINK),
                onClick = onInsertLink,
                contentDescription = "Insert or Edit Link"
            )
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    val extended = LaterBoxThemeTokens.extended
    val bgColor = if (isActive) extended.interactiveAccent.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent
    val tintColor = if (isActive) extended.interactiveAccent else extended.primaryBlack

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StyleChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    val extended = LaterBoxThemeTokens.extended
    val bgColor = if (isActive) extended.interactiveAccent.copy(alpha = 0.15f) else extended.surfaceSecondary
    val textColor = if (isActive) extended.interactiveAccent else extended.primaryBlack

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, if (isActive) extended.interactiveAccent.copy(alpha = 0.4f) else extended.border, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
private fun ToolbarDivider() {
    val extended = LaterBoxThemeTokens.extended
    Spacer(modifier = Modifier.width(4.dp))
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(extended.border)
    )
    Spacer(modifier = Modifier.width(4.dp))
}
