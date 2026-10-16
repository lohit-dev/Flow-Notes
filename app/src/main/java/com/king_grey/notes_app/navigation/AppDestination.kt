package com.king_grey.notes_app.navigation

import androidx.navigation3.runtime.NavKey


sealed interface AppDestination : NavKey {
    data object Notes : AppDestination
    data object Search : AppDestination
    data class NoteEditor(val noteId: Long, val isNew: Boolean = false) : AppDestination
    data object Folders : AppDestination
    data class FolderDetails(val folderId: Long) : AppDestination
    data object RecentlyDeleted : AppDestination
    data class DeletedNotePreview(val noteId: Long) : AppDestination
    data object Settings : AppDestination
    data object PrivacyPolicy : AppDestination
}
