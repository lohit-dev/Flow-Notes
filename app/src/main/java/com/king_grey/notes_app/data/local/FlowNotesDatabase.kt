package com.king_grey.notes_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.king_grey.notes_app.data.local.dao.FolderDao
import com.king_grey.notes_app.data.local.dao.NoteDao
import com.king_grey.notes_app.data.local.entity.FolderEntity
import com.king_grey.notes_app.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DatabaseConverters::class)
abstract class FlowNotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        const val NAME = "flow_notes.db"
    }
}
