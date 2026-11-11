package com.laterbox.ui.screens.note.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.data.model.richnote.ListType
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.data.model.richnote.RichTextFormatter
import com.laterbox.data.model.richnote.TextAlignment
import com.laterbox.data.model.richnote.TextStyleType
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun RichTextBlockItem(
    block: RichBlock.Text,
    isFocused: Boolean,
    onTextChange: (newText: String, cursorPosition: Int) -> Unit,
    onSelectionChange: (start: Int, end: Int) -> Unit,
    onSplitBlock: (cursorPosition: Int) -> Unit,
    onMergeWithPrevious: () -> Unit,
    onToggleChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val focusRequester = remember { FocusRequester() }

    var textFieldValue by remember(block.id) {
        mutableStateOf(TextFieldValue(text = block.text, selection = TextRange(block.text.length)))
    }

    // Keep internal text state synchronized if changed externally (e.g. undo/redo)
    LaunchedEffect(block.text) {
        if (textFieldValue.text != block.text) {
            val newCursor = textFieldValue.selection.start.coerceIn(0, block.text.length)
            textFieldValue = TextFieldValue(text = block.text, selection = TextRange(newCursor))
        }
    }

    val textAlign = when (block.alignment) {
        TextAlignment.LEFT -> TextAlign.Start
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.RIGHT -> TextAlign.End
    }

    val baseFontSize = when (block.style) {
        TextStyleType.TITLE -> 22.sp
        TextStyleType.HEADING_1 -> 19.sp
        TextStyleType.HEADING_2 -> 16.5.sp
        TextStyleType.NORMAL -> 15.sp
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
        label = "textColorAnimation"
    )

    val textStyle = TextStyle(
        fontSize = baseFontSize,
        fontWeight = fontWeight,
        lineHeight = (baseFontSize.value * 1.45f).sp,
        textAlign = textAlign,
        textDecoration = textDecoration,
        color = textColor
    )

    // Visual transformation to display formatted spans live
    val visualTransformation = remember(block.spans, extended.interactiveAccent) {
        VisualTransformation { original ->
            val annotated = RichTextFormatter.toAnnotatedString(
                text = original.text,
                spans = block.spans,
                linkColor = extended.interactiveAccent
            )
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Prefix based on listType
        when (block.listType) {
            ListType.CHECKLIST -> {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onToggleChecklist()
                        }
                        .padding(end = 6.dp, top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (block.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = if (block.isChecked) "Completed item" else "Incomplete item",
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
                // No prefix
            }
        }

        // Editable text input field
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val prevText = textFieldValue.text
                val prevCursor = textFieldValue.selection.start
                textFieldValue = newValue

                if (newValue.text != prevText) {
                    // Check if Enter key was inserted directly into the text stream
                    if (newValue.text.contains("\n")) {
                        val newlinePos = newValue.text.indexOf("\n")
                        val cleanText = newValue.text.replace("\n", "")
                        textFieldValue = TextFieldValue(cleanText, TextRange(newlinePos))
                        onTextChange(cleanText, newlinePos)
                        onSplitBlock(newlinePos)
                    } else {
                        onTextChange(newValue.text, newValue.selection.start)
                    }
                } else if (newValue.selection != TextRange(prevCursor)) {
                    onSelectionChange(newValue.selection.start, newValue.selection.end)
                }
            },
            textStyle = textStyle,
            cursorBrush = SolidColor(extended.primaryBlack),
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            keyboardActions = KeyboardActions(
                onDone = { onSplitBlock(textFieldValue.selection.start) }
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onSelectionChange(textFieldValue.selection.start, textFieldValue.selection.end)
                    }
                }
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter) {
                        onSplitBlock(textFieldValue.selection.start)
                        true
                    } else if (keyEvent.key == Key.Backspace && textFieldValue.selection.start == 0 && textFieldValue.selection.end == 0) {
                        onMergeWithPrevious()
                        true
                    } else {
                        false
                    }
                }
        )
    }
}
