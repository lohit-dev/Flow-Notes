package com.king_grey.notes_app.data.repositories

import com.king_grey.notes_app.data.local.dao.NoteDao
import com.king_grey.notes_app.data.mappers.toEntity
import com.king_grey.notes_app.data.mappers.toModel
import com.king_grey.notes_app.data.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
) : NotesRepository {

    override val notes: Flow<List<Note>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun get(id: Long): Note? = dao.getById(id)?.toModel()

    override suspend fun save(note: Note) = dao.upsert(note.toEntity())

    override suspend fun setPinned(ids: Collection<Long>, pinned: Boolean) {
        if (ids.isNotEmpty()) dao.setPinned(ids.toList(), pinned)
    }

    override suspend fun moveToFolder(ids: Collection<Long>, folderId: Long?) {
        if (ids.isNotEmpty()) dao.moveToFolder(ids.toList(), folderId)
    }

    override suspend fun trash(ids: Collection<Long>) {
        if (ids.isNotEmpty()) dao.setDeletedAt(ids.toList(), System.currentTimeMillis())
    }

    override suspend fun restore(ids: Collection<Long>) {
        if (ids.isNotEmpty()) dao.setDeletedAt(ids.toList(), null)
    }

    override suspend fun deleteForever(ids: Collection<Long>) {
        if (ids.isNotEmpty()) dao.delete(ids.toList())
    }

    override suspend fun purgeExpired(retentionDays: Int) {
        dao.purgeTrashedBefore(System.currentTimeMillis() - retentionDays * 24L * 60 * 60 * 1000)
    }
}
