package com.taskflow.service

import com.taskflow.dto.CreateTaskRequest
import com.taskflow.dto.PageResponse
import com.taskflow.dto.TaskResponseDto
import com.taskflow.dto.UpdateTaskRequest
import com.taskflow.dto.UpdateTaskStatusRequest
import com.taskflow.exception.BadRequestException
import com.taskflow.exception.ResourceNotFoundException
import com.taskflow.exception.UnauthorizedActionException
import com.taskflow.mapper.TaskMapper
import com.taskflow.model.Task
import com.taskflow.model.TaskPriority
import com.taskflow.model.TaskStatus
import com.taskflow.model.User
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.UserRepository
import com.taskflow.strategy.TaskFilterStrategy
import com.taskflow.strategy.TaskSortStrategy
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val projectService: ProjectService,
    private val taskMapper: TaskMapper,
    private val filterStrategy: TaskFilterStrategy,
    private val sortStrategy: TaskSortStrategy
) {

    @Transactional(readOnly = true)
    fun getTasksForProject(
        projectId: Long,
        currentUserId: Long,
        status: TaskStatus?,
        priority: TaskPriority?,
        assigneeId: Long?,
        sortBy: String?,
        sortDirection: String?,
        page: Int,
        size: Int
    ): PageResponse<TaskResponseDto> {
        // Enforce ownership: user must own the project
        projectService.getVerifiedProject(projectId, currentUserId)

        // Apply Strategy Patterns: Dynamic Filter & Sorting
        val specification = filterStrategy.buildSpecification(projectId, status, priority, assigneeId)
        val sort = sortStrategy.resolveSort(sortBy, sortDirection)
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100), sort)

        val taskPage = taskRepository.findAll(specification, pageable)
        val dtoList = taskMapper.toResponseDtoList(taskPage.content)

        return PageResponse(
            content = dtoList,
            pageNumber = taskPage.number,
            pageSize = taskPage.size,
            totalElements = taskPage.totalElements,
            totalPages = taskPage.totalPages,
            isLast = taskPage.isLast
        )
    }

    @Transactional
    fun createTask(
        projectId: Long,
        currentUserId: Long,
        request: CreateTaskRequest
    ): TaskResponseDto {
        val project = projectService.getVerifiedProject(projectId, currentUserId)

        // Rule: dueDate not in the past on create
        if (request.dueDate != null && request.dueDate.isBefore(LocalDate.now())) {
            throw BadRequestException("Due date cannot be in the past when creating a task")
        }

        val assignee: User? = request.assigneeId?.let {
            userRepository.findById(it)
                .orElseThrow { ResourceNotFoundException("Assignee not found with id: $it") }
        }

        val task = taskRepository.save(
            Task(
                title = request.title.trim(),
                description = request.description?.trim(),
                status = TaskStatus.TODO,
                priority = request.priority,
                dueDate = request.dueDate,
                assignee = assignee,
                project = project
            )
        )

        return taskMapper.toResponseDto(task)
    }

    @Transactional
    fun updateTask(
        taskId: Long,
        currentUserId: Long,
        request: UpdateTaskRequest
    ): TaskResponseDto {
        val task = getVerifiedTask(taskId, currentUserId)

        val assignee: User? = request.assigneeId?.let {
            userRepository.findById(it)
                .orElseThrow { ResourceNotFoundException("Assignee not found with id: $it") }
        }

        task.title = request.title.trim()
        task.description = request.description?.trim()
        task.status = request.status
        task.priority = request.priority
        task.dueDate = request.dueDate
        task.assignee = assignee

        val updatedTask = taskRepository.save(task)
        return taskMapper.toResponseDto(updatedTask)
    }

    @Transactional
    fun updateTaskStatus(
        taskId: Long,
        currentUserId: Long,
        request: UpdateTaskStatusRequest
    ): TaskResponseDto {
        val task = getVerifiedTask(taskId, currentUserId)
        task.status = request.status

        val updatedTask = taskRepository.save(task)
        return taskMapper.toResponseDto(updatedTask)
    }

    @Transactional
    fun deleteTask(taskId: Long, currentUserId: Long) {
        val task = getVerifiedTask(taskId, currentUserId)
        taskRepository.delete(task)
    }

    /**
     * Finds task by ID and verifies that the current user owns the parent project.
     */
    private fun getVerifiedTask(taskId: Long, currentUserId: Long): Task {
        val task = taskRepository.findById(taskId)
            .orElseThrow { ResourceNotFoundException("Task not found with id: $taskId") }

        if (task.project.owner.id != currentUserId) {
            throw UnauthorizedActionException("Access denied: You do not own the project for task id $taskId")
        }

        return task
    }
}

