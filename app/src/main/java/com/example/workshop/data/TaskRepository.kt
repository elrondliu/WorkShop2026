package com.example.workshop.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Simple in-memory repository. Swap this implementation for a Room-backed
 * source without touching the ViewModel or the UI.
 */
class TaskRepository {

    private val _tasks = MutableStateFlow<List<Task>>(sampleTasks())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun addTask(task: Task) {
        _tasks.update { current -> listOf(task) + current }
    }

    fun updateTask(task: Task) {
        _tasks.update { current -> current.map { if (it.id == task.id) task else it } }
    }

    fun deleteTask(id: String) {
        _tasks.update { current -> current.filterNot { it.id == id } }
    }

    fun advanceStatus(id: String) {
        _tasks.update { current ->
            current.map {
                if (it.id == id) it.copy(status = it.status.next()) else it
            }
        }
    }

    fun setStatus(id: String, status: TaskStatus) {
        _tasks.update { current ->
            current.map { if (it.id == id) it.copy(status = status) else it }
        }
    }

    private fun TaskStatus.next(): TaskStatus = when (this) {
        TaskStatus.TODO -> TaskStatus.IN_PROGRESS
        TaskStatus.IN_PROGRESS -> TaskStatus.DONE
        TaskStatus.DONE -> TaskStatus.TODO
    }

    private fun sampleTasks(): List<Task> = listOf(
        Task(
            title = "Design the task list screen",
            description = "Wireframe the list, empty state and item swipe actions.",
            priority = TaskPriority.HIGH,
            status = TaskStatus.IN_PROGRESS
        ),
        Task(
            title = "Set up Material 3 theme",
            description = "Colors, typography and shapes for light and dark mode.",
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.DONE
        ),
        Task(
            title = "Write unit tests for the repository",
            priority = TaskPriority.LOW
        ),
        Task(
            title = "Add task persistence",
            description = "Replace the in-memory store with Room.",
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.TODO
        )
    )
}
