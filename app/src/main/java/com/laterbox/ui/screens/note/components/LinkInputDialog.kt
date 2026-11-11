package com.laterbox.ui.screens.note.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laterbox.ui.components.GlassAlertDialog
import com.laterbox.ui.theme.LaterBoxThemeTokens

@Composable
fun LinkInputDialog(
    initialText: String,
    initialUrl: String = "",
    onDismiss: () -> Unit,
    onConfirm: (url: String, text: String) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    var text by remember { mutableStateOf(initialText) }
    var url by remember { mutableStateOf(initialUrl) }
    var isUrlError by remember { mutableStateOf(false) }
    val extended = LaterBoxThemeTokens.extended

    GlassAlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = if (initialUrl.isNotBlank()) "Edit Link" else "Insert Link",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = extended.primaryBlack
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Display Text") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        isUrlError = false
                    },
                    label = { Text("URL (e.g. https://example.com)") },
                    singleLine = true,
                    isError = isUrlError,
                    supportingText = if (isUrlError) {
                        { Text("Please enter a valid URL starting with http:// or https://", color = extended.warmError) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedUrl = url.trim()
                    val formattedUrl = if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                        "https://$trimmedUrl"
                    } else trimmedUrl

                    if (formattedUrl.contains(".") && formattedUrl.length > 5) {
                        onConfirm(formattedUrl, text.trim().ifBlank { formattedUrl })
                    } else {
                        isUrlError = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extended.buttonPrimary,
                    contentColor = extended.buttonPrimaryText
                )
            ) {
                Text("Apply", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row {
                if (onRemove != null && initialUrl.isNotBlank()) {
                    TextButton(
                        onClick = onRemove,
                        colors = ButtonDefaults.textButtonColors(contentColor = extended.warmError)
                    ) {
                        Text("Remove")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = extended.secondaryText)
                }
            }
        }
    )
}
