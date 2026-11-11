package com.laterbox.data.ai

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.laterbox.data.ai.model.GeminiModelInfo
import com.laterbox.data.ai.model.GeminiModels
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.aiDataStore: DataStore<Preferences> by preferencesDataStore(name = "gemini_ai_preferences")

class AiSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_AI_ENABLED = booleanPreferencesKey("is_gemini_ai_enabled")
        val AUTO_ANALYZE = booleanPreferencesKey("auto_analyze_new_items")
        val AUTO_GENERATE_TITLE = booleanPreferencesKey("auto_generate_title")
        val AUTO_GENERATE_SUMMARY = booleanPreferencesKey("auto_generate_summary")
        val AUTO_GENERATE_TAGS = booleanPreferencesKey("auto_generate_tags")
        val REQUIRE_WIFI_FOR_AI = booleanPreferencesKey("require_wifi_for_ai")
        val SELECTED_MODEL_ID = stringPreferencesKey("selected_gemini_model_id")
        val HAS_CONSENTED_PRIVACY = booleanPreferencesKey("has_consented_to_ai_privacy")
        val LAST_TEST_STATUS = stringPreferencesKey("last_api_test_status")
        val LAST_TEST_TIMESTAMP = longPreferencesKey("last_api_test_timestamp")
        val ANALYSIS_COUNT = intPreferencesKey("analysis_count_estimate")
        val DISCOVERED_MODELS_JSON = stringPreferencesKey("discovered_models_json")
        val LAST_MODELS_DISCOVERY_TIMESTAMP = longPreferencesKey("last_models_discovery_timestamp")
    }

    val isAiEnabled: Flow<Boolean> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_AI_ENABLED] ?: false
    }

    val autoAnalyzeNewItems: Flow<Boolean> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_ANALYZE] ?: true
    }

    val autoGenerateTitle: Flow<Boolean> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_GENERATE_TITLE] ?: true
    }

    val autoGenerateSummary: Flow<Boolean> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_GENERATE_SUMMARY] ?: true
    }

    val autoGenerateTags: Flow<Boolean> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_GENERATE_TAGS] ?: true
    }

    val requireWifiForAi: Flow<Boolean> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.REQUIRE_WIFI_FOR_AI] ?: false
    }

    val selectedModelId: Flow<String> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_MODEL_ID] ?: GeminiModels.DEFAULT_MODEL_ID
    }

    val hasConsentedPrivacy: Flow<Boolean> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_CONSENTED_PRIVACY] ?: false
    }

    val lastTestStatus: Flow<String?> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_TEST_STATUS]
    }

    val lastTestTimestamp: Flow<Long> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_TEST_TIMESTAMP] ?: 0L
    }

    val analysisCountEstimate: Flow<Int> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANALYSIS_COUNT] ?: 0
    }

    val discoveredModels: Flow<List<GeminiModelInfo>> = context.aiDataStore.data.map { preferences ->
        val rawJson = preferences[PreferencesKeys.DISCOVERED_MODELS_JSON]
        if (!rawJson.isNullOrBlank()) {
            try {
                val array = JSONArray(rawJson)
                val list = mutableListOf<GeminiModelInfo>()
                for (i in 0 until array.length()) {
                    list.add(GeminiModelInfo.fromJson(array.getJSONObject(i)))
                }
                if (list.isNotEmpty()) list else GeminiModels.FALLBACK_MODELS
            } catch (_: Exception) {
                GeminiModels.FALLBACK_MODELS
            }
        } else {
            GeminiModels.FALLBACK_MODELS
        }
    }

    val lastModelsDiscoveryTimestamp: Flow<Long> = context.aiDataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_MODELS_DISCOVERY_TIMESTAMP] ?: 0L
    }

    suspend fun setAiEnabled(enabled: Boolean) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AI_ENABLED] = enabled
            // If AI is disabled globally, auto-analysis must also be disabled
            if (!enabled) {
                preferences[PreferencesKeys.AUTO_ANALYZE] = false
            }
        }
    }

    suspend fun setAutoAnalyzeNewItems(enabled: Boolean) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_ANALYZE] = enabled
        }
    }

    suspend fun setAutoGenerateTitle(enabled: Boolean) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_GENERATE_TITLE] = enabled
        }
    }

    suspend fun setAutoGenerateSummary(enabled: Boolean) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_GENERATE_SUMMARY] = enabled
        }
    }

    suspend fun setAutoGenerateTags(enabled: Boolean) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_GENERATE_TAGS] = enabled
        }
    }

    suspend fun setRequireWifiForAi(enabled: Boolean) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.REQUIRE_WIFI_FOR_AI] = enabled
        }
    }

    suspend fun setSelectedModelId(modelId: String) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_MODEL_ID] = GeminiModelInfo.normalizeModelId(modelId)
        }
    }

    suspend fun setConsentedPrivacy(consented: Boolean) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_CONSENTED_PRIVACY] = consented
        }
    }

    suspend fun setLastTestResult(status: String, timestamp: Long) {
        context.aiDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TEST_STATUS] = status
            preferences[PreferencesKeys.LAST_TEST_TIMESTAMP] = timestamp
        }
    }

    suspend fun saveDiscoveredModels(models: List<GeminiModelInfo>) {
        context.aiDataStore.edit { preferences ->
            val array = JSONArray()
            models.forEach { array.put(it.toJson()) }
            preferences[PreferencesKeys.DISCOVERED_MODELS_JSON] = array.toString()
            preferences[PreferencesKeys.LAST_MODELS_DISCOVERY_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun incrementAnalysisCount() {
        context.aiDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.ANALYSIS_COUNT] ?: 0
            preferences[PreferencesKeys.ANALYSIS_COUNT] = current + 1
        }
    }

    suspend fun isAiEnabledNow(): Boolean = isAiEnabled.first()
    suspend fun isAutoAnalyzeNow(): Boolean = autoAnalyzeNewItems.first()
    suspend fun getSelectedModelIdNow(): String = selectedModelId.first()
    suspend fun getDiscoveredModelsNow(): List<GeminiModelInfo> = discoveredModels.first()
}
