package com.laterbox.ui.screens.note.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.data.model.richnote.ListType
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.model.richnote.RichDocument
import com.laterbox.data.model.richnote.RichTextFormatter
import com.laterbox.data.model.richnote.TextAlignment
import com.laterbox.data.model.richnote.TextStyleType
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun RichNoteReadingView(
    document: RichDocument,
    onToggleChecklist: (blockId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Document Title
        if (document.title.isNotBlank()) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = 32.sp
                ),
                color = extended.primaryBlack,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Document Blocks
        document.blocks.forEach { block ->
            when (block) {
                is RichBlock.Text -> {
                    val textAlign = when (block.alignment) {
                        TextAlignment.LEFT -> TextAlign.Start
                        TextAlignment.CENTER -> TextAlign.Center
                        TextAlignment.RIGHT -> TextAlign.End
                    }

                    val baseFontSize = when (block.style) {
                        TextStyleType.TITLE -> 22.sp
                        TextStyleType.HEADING_1 -> 19.sp
                        TextStyleType.HEADING_2 -> 16.5.sp
                        TextStyleType.NORMAL -> 15.5.sp
                    }

                    val fontWeight = when (block.style) {
                        TextStyleType.TITLE -> FontWeight.Bold
                        TextStyleType.HEADING_1 -> FontWeight.Bold
                        TextStyleType.HEADING_2 -> FontWeight.SemiBold
                        TextStyleType.NORMAL -> FontWeight.Normal
                    }

                    val textDecoration = if (block.listType == ListType.CHECKLIST && block.isChecked) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }

                    val textColor by animateColorAsState(
                        targetValue = if (block.listType == ListType.CHECKLIST && block.isChecked) {
                            extended.secondaryText
                        } else {
                            extended.primaryBlack
                        },
                        label = "textColor"
                    )

                    val textStyle = TextStyle(
                        fontSize = baseFontSize,
                        fontWeight = fontWeight,
                        lineHeight = (baseFontSize.value * 1.5f).sp,
                        textAlign = textAlign,
                        textDecoration = textDecoration,
                        color = textColor
                    )

                    val annotatedText = remember(block.text, block.spans, extended.interactiveAccent) {
                        RichTextFormatter.toAnnotatedString(
                            text = block.text,
                            spans = block.spans,
                            linkColor = extended.interactiveAccent
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = if (block.style != TextStyleType.NORMAL) 6.dp else 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        when (block.listType) {
                            ListType.CHECKLIST -> {
                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onToggleChecklist(block.id)
                                        }
                                        .padding(end = 6.dp, top = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (block.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = if (block.isChecked) "Completed checklist item" else "Checklist item",
                                        tint = if (block.isChecked) extended.interactiveAccent else extended.secondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            ListType.BULLET -> {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp, end = 10.dp, top = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = extended.primaryBlack
                                    )
                                }
                            }
                            ListType.NUMBERED -> {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp, end = 8.dp, top = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${block.listIndex}.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = extended.secondaryText
                                    )
                                }
                            }
                            ListType.NONE -> {
                                // Standard paragraph
                            }
                        }

                        // Formatted Text with Link Clicking
                        ClickableText(
                            text = annotatedText,
                            style = textStyle,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot open link: ${annotation.item}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is RichBlock.Image -> {
                    RichImageBlockItem(
                        block = block,
                        isReadingMode = true,
                        onCaptionChange = { },
                        onRemove = { }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
