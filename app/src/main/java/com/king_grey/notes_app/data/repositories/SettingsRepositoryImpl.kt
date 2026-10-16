package com.king_grey.notes_app.data.repositories

import com.king_grey.notes_app.data.models.UserSettings
import com.king_grey.notes_app.data.preferences.SettingsDataStore
import com.king_grey.notes_app.ui.screens.settings.AccentColor
import com.king_grey.notes_app.ui.screens.settings.AppearanceMode
import com.king_grey.notes_app.ui.screens.settings.NewNoteStart
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val store: SettingsDataStore,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = store.settings

    override suspend fun setAppearance(value: AppearanceMode) = store.setAppearance(value)
    override suspend fun setAccent(value: AccentColor) = store.setAccent(value)
    override suspend fun setDynamicColor(value: Boolean) = store.setDynamicColor(value)
    override suspend fun setTextSize(value: Int) = store.setTextSize(value)
    override suspend fun setSort(sort: NoteSort, order: SortOrder) {
        store.setSort(sort)
        store.setSortOrder(order)
    }
    override suspend fun setNewNoteStart(value: NewNoteStart) = store.setNewNoteStart(value)
    override suspend fun setShowPreviews(value: Boolean) = store.setShowPreviews(value)
}
