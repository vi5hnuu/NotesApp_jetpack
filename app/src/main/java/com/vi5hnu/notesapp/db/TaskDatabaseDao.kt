package com.vi5hnu.notesapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vi5hnu.notesapp.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TaskDatabaseDao {
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: UUID)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
