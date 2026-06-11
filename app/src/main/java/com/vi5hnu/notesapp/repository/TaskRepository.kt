package com.vi5hnu.notesapp.repository

import com.vi5hnu.notesapp.db.TaskDatabaseDao
import com.vi5hnu.notesapp.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class TaskRepository @Inject constructor(private val dao: TaskDatabaseDao) {
    fun getTasks(): Flow<List<Task>> = dao.getTasks().flowOn(Dispatchers.IO).conflate()

    suspend fun add(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun remove(id: UUID) = dao.delete(id)
    suspend fun removeAll() = dao.deleteAll()
}
