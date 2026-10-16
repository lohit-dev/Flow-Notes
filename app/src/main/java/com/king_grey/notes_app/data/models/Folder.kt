package com.king_grey.notes_app.data.models

import com.king_grey.notes_app.ui.screens.folders.FolderColor

data class Folder(
    val id: Long,
    val name: String,
    val color: FolderColor = FolderColor.Violet,
    /** Order in the Folders tab, lowest first. */
    val position: Int = 0,
)
