package com.vi5hnu.notesapp.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.repository.TaskRepository
import com.vi5hnu.notesapp.utils.nextOccurrence
import com.vi5hnu.notesapp.utils.todayStr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(private val repo: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTasks()
                .distinctUntilChanged()
                .collect { _tasks.value = it }
        }
    }

    fun add(task: Task) = viewModelScope.launch { repo.add(task) }

    fun update(task: Task) = viewModelScope.launch { repo.update(task) }

    fun remove(id: UUID) = viewModelScope.launch { repo.remove(id) }

    fun restore(task: Task) = viewModelScope.launch { repo.add(task) }

    /** Toggle done/undone; handles recur + streak */
    fun toggle(task: Task) {
        viewModelScope.launch {
            if (task.done) {
                repo.update(task.copy(done = false, completedAt = null))
                return@launch
            }
            val today = todayStr()
            if (task.recur != null) {
                val next = nextOccurrence(task.due ?: today, task.recur)
                val snap = task.copy(
                    id = UUID.randomUUID(),
                    done = true,
                    completedAt = today,
                    recur = null,
                    streak = 0,
                    subtasks = "[]"
                )
                repo.add(snap)
                repo.update(
                    task.copy(
                        due = next ?: task.due,
                        streak = task.streak + 1,
                        done = next == null,
                        completedAt = if (next == null) today else null
                    )
                )
            } else {
                repo.update(task.copy(done = true, completedAt = today))
            }
        }
    }

    fun reschedule(task: Task, newDue: String) {
        viewModelScope.launch { repo.update(task.copy(due = newDue)) }
    }
}
