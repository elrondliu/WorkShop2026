package com.example.workshop.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workshop.data.Task
import com.example.workshop.data.TaskPriority
import com.example.workshop.data.TaskRepository
import com.example.workshop.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ALL("All"),
    TODO("To do"),
    IN_PROGRESS("In progress"),
    DONE("Done")
}

data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    val titleError: String? = null
) {
    val isValid: Boolean get() = title.isNotBlank()
}

class TaskViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter.asStateFlow()

    val allTasks: StateFlow<List<Task>> = repository.tasks

    val filteredTasks: StateFlow<List<Task>> = combine(repository.tasks, _filter) { tasks, filter ->
        when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.TODO -> tasks.filter { it.status == TaskStatus.TODO }
            TaskFilter.IN_PROGRESS -> tasks.filter { it.status == TaskStatus.IN_PROGRESS }
            TaskFilter.DONE -> tasks.filter { it.status == TaskStatus.DONE }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<TaskStats> = repository.tasks
        .map { tasks ->
            TaskStats(
                total = tasks.size,
                completed = tasks.count { it.status == TaskStatus.DONE },
                inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskStats())

    private val _addTaskState = MutableStateFlow(AddTaskUiState())
    val addTaskState: StateFlow<AddTaskUiState> = _addTaskState.asStateFlow()

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun toggleDone(task: Task) {
        val next = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
        repository.setStatus(task.id, next)
    }

    fun cycleStatus(task: Task) {
        repository.advanceStatus(task.id)
    }

    fun deleteTask(task: Task) {
        repository.deleteTask(task.id)
    }

    fun onTitleChange(value: String) {
        _addTaskState.value = _addTaskState.value.copy(title = value, titleError = null)
    }

    fun onDescriptionChange(value: String) {
        _addTaskState.value = _addTaskState.value.copy(description = value)
    }

    fun onPriorityChange(priority: TaskPriority) {
        _addTaskState.value = _addTaskState.value.copy(priority = priority)
    }

    fun onStatusChange(status: TaskStatus) {
        _addTaskState.value = _addTaskState.value.copy(status = status)
    }

    /** Returns true when the task was created so the caller can navigate back. */
    fun submitNewTask(): Boolean {
        val state = _addTaskState.value
        if (!state.isValid) {
            _addTaskState.value = state.copy(titleError = "Task title cannot be empty")
            return false
        }
        viewModelScope.launch {
            repository.addTask(
                Task(
                    title = state.title.trim(),
                    description = state.description.trim(),
                    priority = state.priority,
                    status = state.status
                )
            )
        }
        _addTaskState.value = AddTaskUiState()
        return true
    }

    fun resetAddTaskForm() {
        _addTaskState.value = AddTaskUiState()
    }
}

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val inProgress: Int = 0
) {
    val remaining: Int get() = total - completed
    val todo: Int get() = (total - completed - inProgress).coerceAtLeast(0)
}
