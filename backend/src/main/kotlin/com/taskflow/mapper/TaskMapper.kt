package com.taskflow.mapper

import com.taskflow.dto.TaskResponseDto
import com.taskflow.dto.UserSummaryDto
import com.taskflow.model.Task
import com.taskflow.model.TaskStatus
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Design Pattern: Factory / Mapper Pattern
 * Encapsulates conversion from Task JPA entity to TaskResponseDto,
 * safely isolating internal database structure and calculating transient properties (e.g. isOverdue).
 */
@Component
class TaskMapper {

    fun toResponseDto(task: Task, today: LocalDate = LocalDate.now()): TaskResponseDto {
        val isOverdue = task.status != TaskStatus.DONE &&
                task.dueDate != null &&
                task.dueDate!!.isBefore(today)

        val assigneeDto = task.assignee?.let {
            UserSummaryDto(
                id = it.id!!,
                name = it.name,
                email = it.email
            )
        }

        return TaskResponseDto(
            id = task.id!!,
            title = task.title,
            description = task.description,
            status = task.status,
            priority = task.priority,
            dueDate = task.dueDate,
            isOverdue = isOverdue,
            assignee = assigneeDto,
            projectId = task.project.id!!,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )
    }

    fun toResponseDtoList(tasks: List<Task>, today: LocalDate = LocalDate.now()): List<TaskResponseDto> {
        return tasks.map { toResponseDto(it, today) }
    }
}

