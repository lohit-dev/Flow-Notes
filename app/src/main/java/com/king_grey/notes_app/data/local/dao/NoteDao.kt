package com.king_grey.notes_app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.king_grey.notes_app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /** Every note, including the ones in Recently deleted. The UI splits them. */
    @Query("SELECT * FROM notes")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("UPDATE notes SET pinned = :pinned WHERE id IN (:ids)")
    suspend fun setPinned(ids: List<Long>, pinned: Boolean)

    @Query("UPDATE notes SET folder_id = :folderId WHERE id IN (:ids)")
    suspend fun moveToFolder(ids: List<Long>, folderId: Long?)

    @Query("UPDATE notes SET deleted_at = :deletedAt WHERE id IN (:ids)")
    suspend fun setDeletedAt(ids: List<Long>, deletedAt: Long?)

    @Query("UPDATE notes SET deleted_at = :deletedAt WHERE folder_id = :folderId AND deleted_at IS NULL")
    suspend fun trashInFolder(folderId: Long, deletedAt: Long)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    /** Removes notes that have been in Recently deleted since before [cutoff]. */
    @Query("DELETE FROM notes WHERE deleted_at IS NOT NULL AND deleted_at < :cutoff")
    suspend fun purgeTrashedBefore(cutoff: Long)
}
