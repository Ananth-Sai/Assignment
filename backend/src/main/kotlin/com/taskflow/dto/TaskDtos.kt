package com.taskflow.dto

import com.taskflow.model.TaskPriority
import com.taskflow.model.TaskStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate

data class CreateTaskRequest(
    @field:NotBlank(message = "Task title is required")
    @field:Size(max = 120, message = "Task title cannot exceed 120 characters")
    val title: String,

    val description: String? = null,

    val priority: TaskPriority = TaskPriority.MEDIUM,

    val dueDate: LocalDate? = null,

    val assigneeId: Long? = null
)

data class UpdateTaskRequest(
    @field:NotBlank(message = "Task title is required")
    @field:Size(max = 120, message = "Task title cannot exceed 120 characters")
    val title: String,

    val description: String? = null,

    @field:NotNull(message = "Task status is required")
    val status: TaskStatus,

    @field:NotNull(message = "Task priority is required")
    val priority: TaskPriority,

    val dueDate: LocalDate? = null,

    val assigneeId: Long? = null
)

data class UpdateTaskStatusRequest(
    @field:NotNull(message = "Status cannot be null")
    val status: TaskStatus
)

data class TaskResponseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: LocalDate?,
    val isOverdue: Boolean,
    val assignee: UserSummaryDto?,
    val projectId: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class PageResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean
)

