package com.king_grey.notes_app.data.repositories

import com.king_grey.notes_app.data.preferences.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val store: SettingsDataStore,
) : SearchHistoryRepository {
    override val recent: Flow<List<String>> = store.recentSearches
    override suspend fun add(term: String) = store.addRecentSearch(term)
    override suspend fun clear() = store.clearRecentSearches()
}
