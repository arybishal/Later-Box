package com.laterbox.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.LaterBoxApp
import com.laterbox.data.ai.model.AiConnectionState
import com.laterbox.data.ai.model.GeminiModels
import com.laterbox.data.ai.network.GeminiApiClient
import com.laterbox.data.ai.network.GeminiApiResult
import com.laterbox.data.ai.security.SecureApiKeyStorage
import com.laterbox.ui.components.ConfirmationDialog
import com.laterbox.ui.theme.LaterBoxThemeTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSettingsScreen(
    onBack: () -> Unit
) {
    val extended = LaterBoxThemeTokens.extended
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val aiSettings = LaterBoxApp.instance.aiSettingsRepository
    val secureStorage = remember { SecureApiKeyStorage.getInstance(context) }
    val apiClient = remember { GeminiApiClient() }

    val isAiEnabled by aiSettings.isAiEnabled.collectAsState(initial = false)
    val autoAnalyze by aiSettings.autoAnalyzeNewItems.collectAsState(initial = false)
    val selectedModelId by aiSettings.selectedModelId.collectAsState(initial = GeminiModels.DEFAULT_MODEL_ID)
    val analysisCount by aiSettings.analysisCountEstimate.collectAsState(initial = 0)
    val lastTestStatus by aiSettings.lastTestStatus.collectAsState(initial = null)
    val lastTestTimestamp by aiSettings.lastTestTimestamp.collectAsState(initial = 0L)

    var hasKey by remember { mutableStateOf(secureStorage.hasApiKey()) }
    var maskedKey by remember { mutableStateOf(secureStorage.getMaskedApiKey() ?: "") }
    var inputKey by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isEditingKey by remember { mutableStateOf(!hasKey) }

    var testState by remember { mutableStateOf(AiConnectionState.IDLE) }
    var testMessage by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    var showDeleteKeyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = extended.softYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = extended.primaryBlack
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
            // 1. STATUS BADGE CARD
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(extended.pillInactive)
                    .border(1.dp, extended.border, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAiEnabled && hasKey) "Gemini Intelligence Active" else if (!hasKey) "API Key Required" else "Gemini AI Disabled",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = extended.primaryBlack
                        )
                        Text(
                            text = if (isAiEnabled && hasKey) "Model: ${GeminiModels.findModel(selectedModelId).displayName}" else "Configure below to enable smart summaries",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAiEnabled && hasKey) extended.softYellow else extended.cardBackground)
                            .border(1.dp, extended.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAiEnabled && hasKey) "ENABLED" else "OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAiEnabled && hasKey) Color(0xFF111111) else extended.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 2. API KEY MANAGEMENT SECTION
            // =========================================================================
            Text(
                text = "Google Gemini API Key",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = extended.primaryBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Provide your Google AI Studio API key. The key is encrypted with Android Keystore hardware security and stored exclusively on your device.",
                style = MaterialTheme.typography.bodySmall,
                color = extended.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (hasKey && !isEditingKey) {
                // Display Masked Key State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(extended.cardBackground)
                        .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = extended.primaryBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = maskedKey,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = extended.primaryBlack,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                inputKey = ""
                                isEditingKey = true
                            }) {
                                Text("Replace", color = extended.primaryBlack, fontWeight = FontWeight.SemiBold)
                            }

                            IconButton(onClick = { showDeleteKeyDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove key",
                                    tint = extended.warmError
                                )
                            }
                        }
                    }
                }
            } else {
                // Key Input Field
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Enter Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide key" else "Show key",
                                    tint = extended.textSecondary
                                )
                            }

                            IconButton(onClick = {
                                val text = clipboardManager.getText()?.text ?: ""
                                if (text.isNotBlank()) {
                                    inputKey = text.trim()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = extended.primaryBlack
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extended.primaryBlack,
                        cursorColor = extended.primaryBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (hasKey) {
                        TextButton(onClick = { isEditingKey = false }) {
                            Text("Cancel", color = extended.textSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            val clean = inputKey.trim()
                            if (clean.isNotBlank()) {
                                secureStorage.saveApiKey(clean)
                                hasKey = true
                                maskedKey = secureStorage.getMaskedApiKey() ?: ""
                                isEditingKey = false
                                inputKey = ""
                                Toast.makeText(context, "API Key saved securely", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = inputKey.trim().isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extended.primaryBlack,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Key")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 3. MODEL SELECTION SECTION
            // =========================================================================
            Text(
                text = "Gemini Model",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = extended.primaryBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select which Gemini model to use for analyzing saved items.",
                style = MaterialTheme.typography.bodySmall,
                color = extended.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            GeminiModels.ALL_MODELS.forEach { model ->
                val isSelected = model.id == selectedModelId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) extended.pillInactive else extended.cardBackground)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) extended.primaryBlack else extended.border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            scope.launch {
                                aiSettings.setSelectedModelId(model.id)
                            }
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                scope.launch {
                                    aiSettings.setSelectedModelId(model.id)
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = extended.primaryBlack
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = model.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = extended.primaryBlack
                                )

                                if (model.isRecommended) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(extended.softYellow)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "RECOMMENDED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111111)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = model.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 4. TEST CONNECTION SECTION
            // =========================================================================
            Text(
                text = "Connection Diagnostic",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = extended.primaryBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sends a minimal ping to verify that your API key is authentic, valid, and active for $selectedModelId.",
                style = MaterialTheme.typography.bodySmall,
                color = extended.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extended.softYellow)
                    .clickable(enabled = hasKey && !isTesting) {
                        isTesting = true
                        testState = AiConnectionState.TESTING
                        testMessage = "Testing connection to Google Gemini API..."

                        scope.launch {
                            val key = secureStorage.getApiKey() ?: ""
                            val result = apiClient.testConnection(key, selectedModelId)

                            when (result) {
                                is GeminiApiResult.Success -> {
                                    testState = AiConnectionState.SUCCESS
                                    testMessage = result.data
                                    aiSettings.setLastTestResult("SUCCESS", System.currentTimeMillis())
                                }
                                is GeminiApiResult.Error -> {
                                    testState = result.connectionState
                                    testMessage = result.message
                                    aiSettings.setLastTestResult("FAILED", System.currentTimeMillis())
                                }
                            }
                            isTesting = false
                        }
                    }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTesting) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = Color(0xFF111111),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Testing API connection...",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF111111)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test API Connection",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF111111)
                        )
                    }
                }
            }

            if (testMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (testState == AiConnectionState.SUCCESS) extended.pillInactive else extended.pillInactive)
                        .border(
                            1.dp,
                            if (testState == AiConnectionState.SUCCESS) extended.primaryBlack else extended.warmError,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = if (testState == AiConnectionState.SUCCESS) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (testState == AiConnectionState.SUCCESS) extended.primaryBlack else extended.warmError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (testState == AiConnectionState.SUCCESS) "Connection Verified" else "Connection Failed",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (testState == AiConnectionState.SUCCESS) extended.primaryBlack else extended.warmError
                            )
                            Text(
                                text = testMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 5. GLOBAL AI CONTROLS SECTION
            // =========================================================================
            Text(
                text = "Preferences & Controls",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = extended.primaryBlack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Global AI Enable Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Gemini AI",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Text(
                        text = "Allows manual content analysis in the Saved Item Detail screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                Switch(
                    checked = isAiEnabled && hasKey,
                    onCheckedChange = { checked ->
                        if (hasKey) {
                            scope.launch {
                                aiSettings.setAiEnabled(checked)
                            }
                        } else {
                            Toast.makeText(context, "Please configure an API key first.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = hasKey,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = extended.primaryBlack,
                        uncheckedThumbColor = extended.textSecondary,
                        uncheckedTrackColor = extended.pillInactive
                    )
                )
            }

            // Automatic Analysis Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Analyze New Saves",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Text(
                        text = "Automatically schedules an AI analysis in the background when a new item is saved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                Switch(
                    checked = autoAnalyze && isAiEnabled && hasKey,
                    onCheckedChange = { checked ->
                        if (isAiEnabled && hasKey) {
                            scope.launch {
                                aiSettings.setAutoAnalyzeNewItems(checked)
                            }
                        } else {
                            Toast.makeText(context, "Please enable Gemini AI first.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isAiEnabled && hasKey,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = extended.primaryBlack,
                        uncheckedThumbColor = extended.textSecondary,
                        uncheckedTrackColor = extended.pillInactive
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity / Count Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extended.pillInactive)
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = extended.primaryBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTIVITY & COST CONTROL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.primaryBlack,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total Items Analyzed: $analysisCount",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This count is a local estimate of app activity. Google API usage is billed or limited directly by Google AI Studio according to your plan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showDeleteKeyDialog) {
        ConfirmationDialog(
            title = "Remove API Key?",
            message = "Your saved Gemini API key will be deleted from secure storage. AI summaries and analysis features will be disabled until a new key is added.",
            confirmLabel = "Remove Key",
            isDestructive = true,
            onConfirm = {
                secureStorage.clearApiKey()
                hasKey = false
                maskedKey = ""
                isEditingKey = true
                scope.launch {
                    aiSettings.setAiEnabled(false)
                    aiSettings.setAutoAnalyzeNewItems(false)
                }
                showDeleteKeyDialog = false
                Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showDeleteKeyDialog = false }
        )
    }
}
