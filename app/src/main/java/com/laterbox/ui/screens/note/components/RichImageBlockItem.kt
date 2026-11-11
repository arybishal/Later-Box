package com.laterbox.ui.screens.note.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.laterbox.data.model.richnote.RichBlock
import com.laterbox.ui.theme.LaterBoxThemeTokens
import java.io.File

@Composable
fun RichImageBlockItem(
    block: RichBlock.Image,
    isReadingMode: Boolean,
    onCaptionChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val imageFile = File(block.localPath)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                .background(extended.surfaceSecondary)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (imageFile.exists()) imageFile else block.localPath)
                    .crossfade(true)
                    .build(),
                contentDescription = block.caption.ifBlank { "Embedded Note Image" },
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = extended.secondaryText,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(extended.surfaceSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = extended.secondaryText,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Image unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.secondaryText
                            )
                        }
                    }
                }
            )

            // Remove Button (Editing Mode Only)
            if (!isReadingMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.defaultMinSize(minWidth = 36.dp, minHeight = 36.dp).size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Caption
        if (!isReadingMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = block.caption,
                    onValueChange = onCaptionChange,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = extended.secondaryText,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(extended.primaryBlack),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (block.caption.isEmpty()) {
                            Text(
                                text = "Add a caption…",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = extended.tertiaryText,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                )
            }
        } else if (block.caption.isNotBlank()) {
            Text(
                text = block.caption,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp
                ),
                color = extended.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 6.dp)
            )
        }
    }
}
