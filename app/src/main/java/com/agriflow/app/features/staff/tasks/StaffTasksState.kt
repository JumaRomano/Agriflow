package com.agriflow.app.features.staff.tasks

import com.agriflow.app.features.staff.auth.AssignmentsDto

data class StaffTasksState(
    val pendingTasks: List<AssignmentsDto> = emptyList(),
    val completedTasks: List<AssignmentsDto> = emptyList(),
    val allTasks: List<AssignmentsDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
