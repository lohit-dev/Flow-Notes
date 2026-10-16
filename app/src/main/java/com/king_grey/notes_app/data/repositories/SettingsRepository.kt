package com.king_grey.notes_app.data.repositories

import com.king_grey.notes_app.data.models.UserSettings
import com.king_grey.notes_app.ui.screens.settings.AccentColor
import com.king_grey.notes_app.ui.screens.settings.AppearanceMode
import com.king_grey.notes_app.ui.screens.settings.NewNoteStart
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setAppearance(value: AppearanceMode)
    suspend fun setAccent(value: AccentColor)
    suspend fun setDynamicColor(value: Boolean)
    suspend fun setTextSize(value: Int)
    suspend fun setSort(sort: NoteSort, order: SortOrder)
    suspend fun setNewNoteStart(value: NewNoteStart)
    suspend fun setShowPreviews(value: Boolean)
}
