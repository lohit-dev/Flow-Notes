package com.king_grey.notes_app.data.repositories

import androidx.room.withTransaction
import com.king_grey.notes_app.data.local.FlowNotesDatabase
import com.king_grey.notes_app.data.local.entity.FolderEntity
import com.king_grey.notes_app.data.mappers.toModel
import com.king_grey.notes_app.data.models.Folder
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoldersRepositoryImpl @Inject constructor(
    private val db: FlowNotesDatabase,
) : FoldersRepository {

    private val folderDao = db.folderDao()
    private val noteDao = db.noteDao()

    override val folders: Flow<List<Folder>> = folderDao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun create(name: String, color: FolderColor): Long {
        val next = (folderDao.maxPosition() ?: -1) + 1
        return folderDao.insert(FolderEntity(name = name, color = color, position = next))
    }

    override suspend fun rename(id: Long, name: String) = folderDao.rename(id, name)

    override suspend fun setColor(id: Long, color: FolderColor) = folderDao.setColor(id, color)

    override suspend fun reorder(orderedIds: List<Long>) {
        db.withTransaction { orderedIds.forEachIndexed { index, id -> folderDao.setPosition(id, index) } }
    }

    override suspend fun delete(id: Long, deleteNotes: Boolean) {
        db.withTransaction {
            if (deleteNotes) noteDao.trashInFolder(id, System.currentTimeMillis())
            folderDao.delete(id)
        }
    }
}
