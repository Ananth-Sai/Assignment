package com.taskflow.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateProjectRequest(
    @field:NotBlank(message = "Project name cannot be blank")
    @field:Size(max = 120, message = "Project name cannot exceed 120 characters")
    val name: String,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null
)

data class ProjectResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val owner: UserSummaryDto,
    val createdAt: Instant
)

data class ProjectSummaryDto(
    val projectId: Long,
    val totalTasks: Long,
    val todoCount: Long,
    val inProgressCount: Long,
    val doneCount: Long,
    val overdueCount: Long
)

