package com.king_grey.notes_app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.king_grey.notes_app.data.models.UserSettings
import com.king_grey.notes_app.ui.screens.settings.AccentColor
import com.king_grey.notes_app.ui.screens.settings.AppearanceMode
import com.king_grey.notes_app.ui.screens.settings.NewNoteStart
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

private const val MaxRecentSearches = 8

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Typed wrapper over the Settings DataStore. Raw keys never leave this class. */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val Appearance = stringPreferencesKey("appearance")
        val Accent = stringPreferencesKey("accent")
        val DynamicColor = booleanPreferencesKey("dynamic_color")
        val TextSize = intPreferencesKey("text_size")
        val Sort = stringPreferencesKey("sort")
        val SortOrder = stringPreferencesKey("sort_order")
        val NewNoteStart = stringPreferencesKey("new_note_start")
        val ShowPreviews = booleanPreferencesKey("show_previews")
        val RecentSearches = stringPreferencesKey("recent_searches")
    }

    val settings: Flow<UserSettings> = context.settingsStore.data.map { p ->
        val d = UserSettings()
        UserSettings(
            appearance = p[Keys.Appearance].toEnum(d.appearance),
            accent = p[Keys.Accent].toEnum(d.accent),
            dynamicColor = p[Keys.DynamicColor] ?: d.dynamicColor,
            textSize = p[Keys.TextSize] ?: d.textSize,
            sort = p[Keys.Sort].toEnum(d.sort),
            sortOrder = p[Keys.SortOrder].toEnum(d.sortOrder),
            newNoteStart = p[Keys.NewNoteStart].toEnum(d.newNoteStart),
            showPreviews = p[Keys.ShowPreviews] ?: d.showPreviews,
        )
    }

    /** Newest first, at most [MaxRecentSearches]. Stored as a JSON array in one preference. */
    val recentSearches: Flow<List<String>> = context.settingsStore.data.map { p ->
        runCatching {
            val array = JSONArray(p[Keys.RecentSearches] ?: "[]")
            (0 until array.length()).map { array.getString(it) }
        }.getOrDefault(emptyList())
    }

    suspend fun addRecentSearch(term: String) {
        val clean = term.trim()
        if (clean.isEmpty()) return
        context.settingsStore.edit { p ->
            val current = runCatching {
                val array = JSONArray(p[Keys.RecentSearches] ?: "[]")
                (0 until array.length()).map { array.getString(it) }
            }.getOrDefault(emptyList())
            val next = (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) })
                .take(MaxRecentSearches)
            p[Keys.RecentSearches] = JSONArray(next).toString()
        }
    }

    suspend fun clearRecentSearches() {
        context.settingsStore.edit { it.remove(Keys.RecentSearches) }
    }

    suspend fun setAppearance(v: AppearanceMode) = edit { it[Keys.Appearance] = v.name }
    suspend fun setAccent(v: AccentColor) = edit { it[Keys.Accent] = v.name }
    suspend fun setDynamicColor(v: Boolean) = edit { it[Keys.DynamicColor] = v }
    suspend fun setTextSize(v: Int) = edit { it[Keys.TextSize] = v }
    suspend fun setSort(v: NoteSort) = edit { it[Keys.Sort] = v.name }
    suspend fun setSortOrder(v: SortOrder) = edit { it[Keys.SortOrder] = v.name }
    suspend fun setNewNoteStart(v: NewNoteStart) = edit { it[Keys.NewNoteStart] = v.name }
    suspend fun setShowPreviews(v: Boolean) = edit { it[Keys.ShowPreviews] = v }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.settingsStore.edit { block(it) }
    }

    private inline fun <reified E : Enum<E>> String?.toEnum(default: E): E =
        enumValues<E>().firstOrNull { it.name == this } ?: default
}
