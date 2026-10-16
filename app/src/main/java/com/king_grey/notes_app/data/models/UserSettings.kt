package com.king_grey.notes_app.data.models

import com.king_grey.notes_app.ui.screens.settings.AccentColor
import com.king_grey.notes_app.ui.screens.settings.AppearanceMode
import com.king_grey.notes_app.ui.screens.settings.NewNoteStart
import com.king_grey.notes_app.ui.screens.settings.NoteSort
import com.king_grey.notes_app.ui.screens.settings.SortOrder

/** Everything in Settings that has to survive a restart. */
data class UserSettings(
    val appearance: AppearanceMode = AppearanceMode.System,
    val accent: AccentColor = AccentColor.Sapphire,
    val dynamicColor: Boolean = false,
    val textSize: Int = 17,
    val sort: NoteSort = NoteSort.DateEdited,
    val sortOrder: SortOrder = SortOrder.NewestFirst,
    val newNoteStart: NewNoteStart = NewNoteStart.Title,
    val showPreviews: Boolean = true,
)
