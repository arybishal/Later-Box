package com.laterbox.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laterbox.LaterBoxApp
import com.laterbox.data.ai.model.AiConnectionState
import com.laterbox.data.ai.model.GeminiModelInfo
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
    val autoTitle by aiSettings.autoGenerateTitle.collectAsState(initial = true)
    val autoSummary by aiSettings.autoGenerateSummary.collectAsState(initial = true)
    val autoTags by aiSettings.autoGenerateTags.collectAsState(initial = true)
    val requireWifi by aiSettings.requireWifiForAi.collectAsState(initial = false)
    val selectedModelId by aiSettings.selectedModelId.collectAsState(initial = GeminiModels.DEFAULT_MODEL_ID)
    val discoveredModels by aiSettings.discoveredModels.collectAsState(initial = GeminiModels.FALLBACK_MODELS)
    val lastModelsDiscoveryTimestamp by aiSettings.lastModelsDiscoveryTimestamp.collectAsState(initial = 0L)
    val analysisCount by aiSettings.analysisCountEstimate.collectAsState(initial = 0)
    val lastTestStatus by aiSettings.lastTestStatus.collectAsState(initial = null)
    val lastTestTimestamp by aiSettings.lastTestTimestamp.collectAsState(initial = 0L)

    var hasKey by remember { mutableStateOf(secureStorage.hasApiKey()) }
    var maskedKey by remember { mutableStateOf(secureStorage.getMaskedApiKey() ?: "") }
    var inputKey by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isEditingKey by remember { mutableStateOf(!hasKey) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFamilyTab by remember { mutableStateOf("All") }
    var isRefreshingModels by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }

    var testState by remember { mutableStateOf(AiConnectionState.IDLE) }
    var testMessage by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    var showDeleteKeyDialog by remember { mutableStateOf(false) }

    val cleanSelectedId = GeminiModelInfo.normalizeModelId(selectedModelId)
    val currentSelectedModel = GeminiModels.findModel(cleanSelectedId, discoveredModels)
    val isModelAvailableInProject = discoveredModels.any { it.id.equals(cleanSelectedId, ignoreCase = true) }
    val recommendedModel = GeminiModels.selectBestDefault(discoveredModels)

    val lastRefreshedText = if (lastModelsDiscoveryTimestamp > 0L) {
        val formatter = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        "Updated: ${formatter.format(Date(lastModelsDiscoveryTimestamp))}"
    } else {
        "Default model registry (Offline)"
    }

    val flashCount = discoveredModels.count { it.family == "Flash" }
    val proCount = discoveredModels.count { it.family == "Pro" }
    val otherCount = discoveredModels.count { it.family != "Flash" && it.family != "Pro" }

    val filteredModels = remember(discoveredModels, searchQuery, selectedFamilyTab) {
        discoveredModels.filter { model ->
            val matchesSearch = searchQuery.isBlank() ||
                model.displayName.contains(searchQuery, ignoreCase = true) ||
                model.id.contains(searchQuery, ignoreCase = true) ||
                model.description.contains(searchQuery, ignoreCase = true)

            val matchesFamily = when (selectedFamilyTab) {
                "Flash" -> model.family == "Flash"
                "Pro" -> model.family == "Pro"
                "Other" -> model.family != "Flash" && model.family != "Pro"
                else -> true
            }

            matchesSearch && matchesFamily
        }
    }

    LaunchedEffect(hasKey) {
        if (hasKey) {
            val key = secureStorage.getApiKey() ?: ""
            if (key.isNotBlank()) {
                val res = apiClient.fetchAvailableModels(key)
                if (res is GeminiApiResult.Success && res.data.isNotEmpty()) {
                    aiSettings.saveDiscoveredModels(res.data)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = extended.primaryBlack,
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
                    containerColor = extended.background
                )
            )
        },
        containerColor = extended.background
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
                            text = if (isAiEnabled && hasKey) "Model: ${currentSelectedModel.displayName}" else "Configure below to enable smart summaries",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAiEnabled && hasKey) extended.surfaceSecondary else extended.cardBackground)
                            .border(1.dp, extended.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAiEnabled && hasKey) "ENABLED" else "OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAiEnabled && hasKey) extended.primaryText else extended.secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // 2. SUBSCRIPTION VS DEVELOPER API NOTICE BANNER
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extended.cardBackground)
                    .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = extended.primaryBlack,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google AI Pro vs. Developer API",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = extended.primaryBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your Google AI subscription and Gemini Developer API usage are separate. API access, available models, quotas and charges depend on your Google API project and its configuration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 3. API KEY MANAGEMENT SECTION
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
                                    fontFamily = FontFamily.Monospace,
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
                                    tint = extended.textSecondary
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

                                // Auto-discover available models with the newly saved key
                                scope.launch {
                                    isRefreshingModels = true
                                    val res = apiClient.fetchAvailableModels(clean)
                                    if (res is GeminiApiResult.Success && res.data.isNotEmpty()) {
                                        aiSettings.saveDiscoveredModels(res.data)
                                    }
                                    isRefreshingModels = false
                                }
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
            // 4. MODEL DISCOVERY & SELECTION SECTION
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gemini Models",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = extended.primaryBlack
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${discoveredModels.size} models available · $lastRefreshedText",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                if (hasKey) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val key = secureStorage.getApiKey() ?: ""
                                if (key.isNotBlank()) {
                                    isRefreshingModels = true
                                    when (val result = apiClient.fetchAvailableModels(key)) {
                                        is GeminiApiResult.Success -> {
                                            aiSettings.saveDiscoveredModels(result.data)
                                            Toast.makeText(context, "Discovered ${result.data.size} models from Google API", Toast.LENGTH_SHORT).show()
                                        }
                                        is GeminiApiResult.Error -> {
                                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    isRefreshingModels = false
                                }
                            }
                        },
                        enabled = !isRefreshingModels,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (isRefreshingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = extended.primaryBlack
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetching...", fontSize = 12.sp, color = extended.primaryBlack)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = extended.primaryBlack
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetch Models", fontSize = 12.sp, color = extended.primaryBlack)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Warning if selected model is not available in the project
            if (!isModelAvailableInProject && hasKey) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(extended.pillInactive)
                        .border(1.dp, extended.primaryBlack, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = extended.primaryBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Selected Model Unavailable in Project",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = extended.primaryBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "'$cleanSelectedId' is not present in your API model list. Tap below to switch to the recommended model.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extended.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    aiSettings.setSelectedModelId(recommendedModel.id)
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Switch to ${recommendedModel.displayName} (${recommendedModel.id})", color = extended.primaryBlack)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // =========================================================================
            // MINIMAL MODEL DROPDOWN SELECTOR
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extended.cardBackground)
                    .border(
                        width = if (isModelDropdownExpanded) 1.5.dp else 1.dp,
                        color = if (isModelDropdownExpanded) extended.primaryBlack else extended.border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { isModelDropdownExpanded = !isModelDropdownExpanded }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentSelectedModel.displayName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = extended.primaryBlack
                            )
                            if (currentSelectedModel.isRecommended) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(extended.surfaceSecondary)
                                        .border(1.dp, extended.border, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "RECOMMENDED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = extended.primaryText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currentSelectedModel.id,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = extended.primaryBlack
                            )

                            val tokenLimits = currentSelectedModel.formatTokenLimits()
                            if (tokenLimits.isNotBlank()) {
                                Text(
                                    text = "· $tokenLimits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extended.textSecondary
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isModelDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isModelDropdownExpanded) "Collapse models" else "Expand models",
                        tint = extended.primaryBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Expanded Dropdown List Panel
            if (isModelDropdownExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(extended.cardBackground)
                        .border(1.dp, extended.border, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        // Search Filter Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Filter models (e.g. 2.5, flash, pro)...", fontSize = 13.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = extended.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = extended.textSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = extended.primaryBlack,
                                unfocusedBorderColor = extended.border,
                                cursorColor = extended.primaryBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Family Filter Chips inside Dropdown
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "All" to discoveredModels.size,
                                "Flash" to flashCount,
                                "Pro" to proCount,
                                "Other" to otherCount
                            ).forEach { (family, count) ->
                                val isChipSelected = selectedFamilyTab == family
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isChipSelected) extended.primaryBlack else extended.pillInactive)
                                        .border(1.dp, if (isChipSelected) extended.primaryBlack else extended.border, RoundedCornerShape(6.dp))
                                        .clickable { selectedFamilyTab = family }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$family ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isChipSelected) Color.White else extended.primaryBlack
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = extended.border)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Scrollable List of Models with max height
                        if (filteredModels.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No models match '$searchQuery'" else "No models found for $selectedFamilyTab",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = extended.textSecondary
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                filteredModels.forEach { model ->
                                    val isSelected = model.id.equals(cleanSelectedId, ignoreCase = true)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) extended.pillInactive else Color.Transparent)
                                            .clickable {
                                                scope.launch {
                                                    aiSettings.setSelectedModelId(model.id)
                                                }
                                                isModelDropdownExpanded = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                scope.launch {
                                                    aiSettings.setSelectedModelId(model.id)
                                                }
                                                isModelDropdownExpanded = false
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = extended.primaryBlack
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = model.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = extended.primaryBlack
                                                )

                                                if (model.isRecommended) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(extended.surfaceSecondary)
                                                            .border(1.dp, extended.border, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "RECOMMENDED",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = extended.primaryText
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = model.id,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = extended.textSecondary
                                                )

                                                val tokenLimits = model.formatTokenLimits()
                                                if (tokenLimits.isNotBlank()) {
                                                    Text(
                                                        text = "· $tokenLimits",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                        color = extended.textSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 5. TEST SELECTED MODEL SECTION (6-STEP DIAGNOSTIC)
            // =========================================================================
            Text(
                text = "Test Selected Model",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = extended.primaryBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Executes 6 verification steps for '$cleanSelectedId': key format, models.list discovery, model existence in project, generateContent capability, and a real ping request.",
                style = MaterialTheme.typography.bodySmall,
                color = extended.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (hasKey && !isTesting) extended.interactiveAccent else extended.interactiveAccent.copy(alpha = 0.4f))
                    .clickable(enabled = hasKey && !isTesting) {
                        isTesting = true
                        testState = AiConnectionState.TESTING
                        testMessage = "Testing '$cleanSelectedId' on Google Gemini API..."

                        scope.launch {
                            val key = secureStorage.getApiKey() ?: ""
                            val modelsRes = apiClient.fetchAvailableModels(key)
                            if (modelsRes is GeminiApiResult.Success && modelsRes.data.isNotEmpty()) {
                                aiSettings.saveDiscoveredModels(modelsRes.data)
                            }

                            val result = apiClient.testConnection(key, cleanSelectedId)

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
                            color = extended.buttonPrimaryText,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Testing $cleanSelectedId...",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = extended.buttonPrimaryText
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = extended.buttonPrimaryText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Test Selected Model ($cleanSelectedId)",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = extended.buttonPrimaryText
                        )
                    }
                }
            }

            if (testMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                val isSuccess = testState == AiConnectionState.SUCCESS
                val title = when (testState) {
                    AiConnectionState.SUCCESS -> "Diagnostic Passed (6/6 Checks)"
                    AiConnectionState.REGIONAL_RESTRICTION -> "Regional Restriction Reported by Google"
                    AiConnectionState.API_NOT_ENABLED -> "Generative Language API Disabled"
                    AiConnectionState.API_KEY_RESTRICTED -> "API Key Permission Denied / Restricted"
                    AiConnectionState.MODEL_NOT_FOUND -> "Model Not Accessible in Project"
                    AiConnectionState.MODEL_UNSUPPORTED -> "Model Unsupported for Content Generation"
                    AiConnectionState.QUOTA_EXCEEDED -> "API Quota Exceeded"
                    AiConnectionState.RATE_LIMIT -> "Rate Limit Reached"
                    AiConnectionState.BILLING_RESTRICTION -> "Billing Requirement"
                    AiConnectionState.INVALID_KEY -> "Invalid API Key"
                    AiConnectionState.TIMEOUT -> "Request Timed Out"
                    AiConnectionState.NETWORK_ERROR -> "Network Connection Error"
                    AiConnectionState.SERVER_ERROR -> "Google Gemini Server Error"
                    else -> "Diagnostic Issue Detected"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(extended.pillInactive)
                        .border(
                            1.dp,
                            if (isSuccess) extended.primaryBlack else extended.border,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = extended.primaryBlack,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = extended.primaryBlack
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = testMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = extended.textSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = extended.border)
            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // 6. GLOBAL AI CONTROLS SECTION
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

            // Auto-Generate Title Toggle
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
                        text = "Auto-Generate AI Titles",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Text(
                        text = "Suggests clean, concise titles for quick voice notes, photos, and snips.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                Switch(
                    checked = autoTitle && isAiEnabled && hasKey,
                    onCheckedChange = { checked ->
                        scope.launch { aiSettings.setAutoGenerateTitle(checked) }
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

            // Auto-Generate Summary Toggle
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
                        text = "Auto-Generate AI Summaries",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Text(
                        text = "Creates 2-3 sentence executive summaries and bulleted key takeaways.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                Switch(
                    checked = autoSummary && isAiEnabled && hasKey,
                    onCheckedChange = { checked ->
                        scope.launch { aiSettings.setAutoGenerateSummary(checked) }
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

            // Auto-Generate Tags Toggle
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
                        text = "Auto-Suggest Category Tags",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Text(
                        text = "Identifies relevant topic tags for quick library filtering.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                Switch(
                    checked = autoTags && isAiEnabled && hasKey,
                    onCheckedChange = { checked ->
                        scope.launch { aiSettings.setAutoGenerateTags(checked) }
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

            // Wi-Fi Only Toggle
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
                        text = "Wi-Fi Only for AI Analysis",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = extended.primaryBlack
                    )
                    Text(
                        text = "Conserves mobile data by deferring media uploads to Wi-Fi connections.",
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textSecondary
                    )
                }

                Switch(
                    checked = requireWifi && isAiEnabled && hasKey,
                    onCheckedChange = { checked ->
                        scope.launch { aiSettings.setRequireWifiForAi(checked) }
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

            // Activity / Cost Estimate Card
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
