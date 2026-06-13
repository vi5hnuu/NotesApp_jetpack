package com.vi5hnu.notesapp.repository

import com.vi5hnu.notesapp.db.UserListDao
import com.vi5hnu.notesapp.model.UserList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/** Persistence for user-created lists. Built-in lists are merged in the ViewModel layer. */
class ListRepository @Inject constructor(private val dao: UserListDao) {
    fun getLists(): Flow<List<UserList>> = dao.getAll().flowOn(Dispatchers.IO)

    suspend fun add(list: UserList) = dao.insert(list)

    suspend fun remove(id: String) = dao.delete(id)
}
