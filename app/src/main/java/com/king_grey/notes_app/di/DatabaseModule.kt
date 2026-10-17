package com.king_grey.notes_app.di

import android.content.Context
import androidx.room.Room
import com.king_grey.notes_app.data.local.FlowNotesDatabase
import com.king_grey.notes_app.data.local.dao.FolderDao
import com.king_grey.notes_app.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlowNotesDatabase =
        Room.databaseBuilder(context, FlowNotesDatabase::class.java, FlowNotesDatabase.NAME)
            .addMigrations(*FlowNotesDatabase.MIGRATIONS)
            .build()

    @Provides
    fun provideNoteDao(db: FlowNotesDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideFolderDao(db: FlowNotesDatabase): FolderDao = db.folderDao()
}
