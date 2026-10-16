package com.king_grey.notes_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.king_grey.notes_app.data.local.entity.FolderEntity
import com.king_grey.notes_app.ui.screens.folders.FolderColor
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY position, id")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY position, id")
    suspend fun getAll(): List<FolderEntity>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("UPDATE folders SET color = :color WHERE id = :id")
    suspend fun setColor(id: Long, color: FolderColor)

    @Query("UPDATE folders SET position = :position WHERE id = :id")
    suspend fun setPosition(id: Long, position: Int)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT MAX(position) FROM folders")
    suspend fun maxPosition(): Int?
}
