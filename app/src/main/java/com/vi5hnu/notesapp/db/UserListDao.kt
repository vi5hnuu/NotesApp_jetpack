package com.vi5hnu.notesapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vi5hnu.notesapp.model.UserList
import kotlinx.coroutines.flow.Flow

@Dao
interface UserListDao {
    @Query("SELECT * FROM user_lists ORDER BY name COLLATE NOCASE ASC")
    fun getAll(): Flow<List<UserList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: UserList)

    @Query("DELETE FROM user_lists WHERE id = :id")
    suspend fun delete(id: String)
}
