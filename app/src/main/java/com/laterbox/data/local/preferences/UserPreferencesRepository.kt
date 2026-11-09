package com.laterbox.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.laterbox.data.model.SortOrder
import com.laterbox.data.model.ThemeMode
import com.laterbox.data.model.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val AUTO_FETCH_METADATA = booleanPreferencesKey("auto_fetch_metadata")
        val DIRECT_SAVE_TO_INBOX = booleanPreferencesKey("direct_save_to_inbox")
        val IN_APP_VIEWING = booleanPreferencesKey("in_app_viewing")
        val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
    }

    val lastBackupTimestamp: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP]
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        try {
            ThemeMode.valueOf(preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val viewMode: Flow<ViewMode> = context.dataStore.data.map { preferences ->
        try {
            ViewMode.valueOf(preferences[PreferencesKeys.VIEW_MODE] ?: ViewMode.GRID.name)
        } catch (_: Exception) {
            ViewMode.GRID
        }
    }

    val sortOrder: Flow<SortOrder> = context.dataStore.data.map { preferences ->
        try {
            SortOrder.valueOf(preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NEWEST.name)
        } catch (_: Exception) {
            SortOrder.NEWEST
        }
    }

    val autoFetchMetadata: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_FETCH_METADATA] ?: true
    }

    val directSaveToInbox: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DIRECT_SAVE_TO_INBOX] ?: false
    }

    val inAppViewing: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IN_APP_VIEWING] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setViewMode(mode: ViewMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIEW_MODE] = mode.name
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = order.name
        }
    }

    suspend fun setAutoFetchMetadata(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_FETCH_METADATA] = enabled
        }
    }

    suspend fun setDirectSaveToInbox(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIRECT_SAVE_TO_INBOX] = enabled
        }
    }

    suspend fun setInAppViewing(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IN_APP_VIEWING] = enabled
        }
    }

    suspend fun setLastBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP] = timestamp
        }
    }

    suspend fun getAllPreferencesSnapshot(): Map<String, String> {
        val prefs = context.dataStore.data.firstOrNull() ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        prefs[PreferencesKeys.THEME_MODE]?.let { result["theme_mode"] = it }
        prefs[PreferencesKeys.VIEW_MODE]?.let { result["view_mode"] = it }
        prefs[PreferencesKeys.SORT_ORDER]?.let { result["sort_order"] = it }
        prefs[PreferencesKeys.AUTO_FETCH_METADATA]?.let { result["auto_fetch_metadata"] = it.toString() }
        prefs[PreferencesKeys.DIRECT_SAVE_TO_INBOX]?.let { result["direct_save_to_inbox"] = it.toString() }
        prefs[PreferencesKeys.IN_APP_VIEWING]?.let { result["in_app_viewing"] = it.toString() }
        return result
    }

    suspend fun restorePreferences(map: Map<String, String>) {
        context.dataStore.edit { prefs ->
            map["theme_mode"]?.let { prefs[PreferencesKeys.THEME_MODE] = it }
            map["view_mode"]?.let { prefs[PreferencesKeys.VIEW_MODE] = it }
            map["sort_order"]?.let { prefs[PreferencesKeys.SORT_ORDER] = it }
            map["auto_fetch_metadata"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.AUTO_FETCH_METADATA] = it }
            map["direct_save_to_inbox"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.DIRECT_SAVE_TO_INBOX] = it }
            map["in_app_viewing"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.IN_APP_VIEWING] = it }
        }
    }
}
