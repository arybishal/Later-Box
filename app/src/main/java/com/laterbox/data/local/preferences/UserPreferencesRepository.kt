package com.laterbox.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.laterbox.data.model.SortOrder
import com.laterbox.data.model.ThemeMode
import com.laterbox.data.model.ViewMode
import kotlinx.coroutines.flow.Flow
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
}
