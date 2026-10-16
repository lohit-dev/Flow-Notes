package com.king_grey.notes_app.data.local

import androidx.room.TypeConverter
import com.king_grey.notes_app.ui.screens.folders.FolderColor

class DatabaseConverters {

    @TypeConverter
    fun folderColorToString(color: FolderColor): String = color.name

    @TypeConverter
    fun stringToFolderColor(name: String): FolderColor =
        FolderColor.entries.firstOrNull { it.name == name } ?: FolderColor.Violet
}
