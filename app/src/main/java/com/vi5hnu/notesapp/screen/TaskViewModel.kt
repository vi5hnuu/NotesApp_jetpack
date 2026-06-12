package com.vi5hnu.notesapp.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.notifications.ReminderScheduler
import com.vi5hnu.notesapp.repository.TaskRepository
import com.vi5hnu.notesapp.utils.addDays
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
class TaskViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val reminders: ReminderScheduler
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private var seeded = false

    init {
        // Apply the daily planning nudge from the saved preference at startup.
        reminders.syncDailyNudgeFromPrefs()
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTasks()
                .distinctUntilChanged()
                .collect { list ->
                    if (list.isEmpty() && !seeded) {
                        seeded = true
                        seedInitialData()
                    } else {
                        _tasks.value = list
                        // Keep reminder alarms in sync with the latest tasks.
                        reminders.sync(list)
                    }
                }
        }
    }

    /** Re-evaluate all reminder alarms — call when the reminders setting is toggled. */
    fun syncReminders() = reminders.sync(_tasks.value)

    /** Enable/disable the daily planning nudge — call when the nudge setting is toggled. */
    fun setDailyNudge(enabled: Boolean) = reminders.setDailyNudge(enabled)

    private suspend fun seedInitialData() {
        val today = todayStr()
        listOf(
            Task(title = "Morning workout", listId = "health", due = today, time = "07:00"),
            Task(title = "Design review with the team", notes = "Bring the new flows", listId = "work", due = today, time = "14:30", priority = "high"),
            Task(title = "Plan weekend trip", listId = "home", due = today),
            Task(title = "Water the plants", listId = "home", due = today),
            Task(title = "Buy groceries", listId = "groceries", due = addDays(today, 1)),
            Task(title = "Team standup", listId = "work", due = addDays(today, 1), time = "09:00"),
            Task(title = "Dentist appointment", listId = "health", due = addDays(today, 3), time = "10:00", priority = "med"),
        ).forEach { repo.add(it) }
    }

    fun add(task: Task) = viewModelScope.launch(Dispatchers.IO) { repo.add(task) }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) { repo.update(task) }

    fun remove(id: UUID) = viewModelScope.launch(Dispatchers.IO) { repo.remove(id) }

    fun restore(task: Task) = viewModelScope.launch(Dispatchers.IO) { repo.add(task) }

    /** Toggle done/undone; handles recur + streak */
    fun toggle(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) { repo.update(task.copy(due = newDue)) }
    }
}
