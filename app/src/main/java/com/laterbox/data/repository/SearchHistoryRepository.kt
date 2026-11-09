package com.laterbox.data.repository

import com.laterbox.data.local.dao.SearchHistoryDao
import com.laterbox.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

class SearchHistoryRepository(
    private val searchHistoryDao: SearchHistoryDao
) {

    fun getRecentSearches(limit: Int = 10): Flow<List<String>> =
        searchHistoryDao.getRecentSearchQueries(limit)

    suspend fun recordSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            searchHistoryDao.insertSearchQuery(
                SearchHistoryEntity(query = trimmed, searchedAt = System.currentTimeMillis())
            )
        }
    }

    suspend fun deleteSearch(query: String) {
        searchHistoryDao.deleteSearchQuery(query)
    }

    suspend fun clearHistory() {
        searchHistoryDao.clearSearchHistory()
    }
}
