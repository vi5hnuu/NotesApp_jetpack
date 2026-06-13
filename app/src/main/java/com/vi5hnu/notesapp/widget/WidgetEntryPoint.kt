package com.vi5hnu.notesapp.widget

import com.vi5hnu.notesapp.repository.ListRepository
import com.vi5hnu.notesapp.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Dependencies the widget provider and its list factory need (no `@AndroidEntryPoint` required). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun taskRepository(): TaskRepository
    fun listRepository(): ListRepository
}
