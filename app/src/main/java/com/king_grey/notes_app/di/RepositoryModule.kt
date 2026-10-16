package com.king_grey.notes_app.di

import com.king_grey.notes_app.data.repositories.FoldersRepository
import com.king_grey.notes_app.data.repositories.FoldersRepositoryImpl
import com.king_grey.notes_app.data.repositories.NotesRepository
import com.king_grey.notes_app.data.repositories.NotesRepositoryImpl
import com.king_grey.notes_app.data.repositories.SearchHistoryRepository
import com.king_grey.notes_app.data.repositories.SearchHistoryRepositoryImpl
import com.king_grey.notes_app.data.repositories.SettingsRepository
import com.king_grey.notes_app.data.repositories.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindNotes(impl: NotesRepositoryImpl): NotesRepository

    @Binds @Singleton
    abstract fun bindFolders(impl: FoldersRepositoryImpl): FoldersRepository

    @Binds @Singleton
    abstract fun bindSettings(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindSearchHistory(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository
}
