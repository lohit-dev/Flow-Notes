package com.king_grey.notes_app.data.repositories

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    /** Newest first. Survives restarts. */
    val recent: Flow<List<String>>

    suspend fun add(term: String)
    suspend fun clear()
}
