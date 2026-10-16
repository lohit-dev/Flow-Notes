package com.king_grey.notes_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.king_grey.notes_app.ui.screens.folders.FolderColor

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: FolderColor,
    val position: Int,
)
