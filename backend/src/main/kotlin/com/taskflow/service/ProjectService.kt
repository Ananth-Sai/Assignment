package com.taskflow.service

import com.taskflow.dto.CreateProjectRequest
import com.taskflow.dto.ProjectResponseDto
import com.taskflow.dto.ProjectSummaryDto
import com.taskflow.exception.ResourceNotFoundException
import com.taskflow.exception.UnauthorizedActionException
import com.taskflow.mapper.ProjectMapper
import com.taskflow.model.Project
import com.taskflow.model.TaskStatus
import com.taskflow.model.User
import com.taskflow.repository.ProjectRepository
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val projectMapper: ProjectMapper
) {

    @Transactional(readOnly = true)
    fun getProjectsForUser(userId: Long): List<ProjectResponseDto> {
        val projects = projectRepository.findByOwnerIdOrderByCreatedAtDesc(userId)
        return projectMapper.toResponseDtoList(projects)
    }

    @Transactional
    fun createProject(userId: Long, request: CreateProjectRequest): ProjectResponseDto {
        val owner = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val project = projectRepository.save(
            Project(
                name = request.name.trim(),
                description = request.description?.trim(),
                owner = owner
            )
        )
        return projectMapper.toResponseDto(project)
    }

    @Transactional(readOnly = true)
    fun getProjectSummary(projectId: Long, currentUserId: Long): ProjectSummaryDto {
        // Enforce ownership check
        getVerifiedProject(projectId, currentUserId)

        val todoCount = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.TODO)
        val inProgressCount = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS)
        val doneCount = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE)
        val overdueCount = taskRepository.countOverdueTasks(projectId, LocalDate.now())

        return ProjectSummaryDto(
            projectId = projectId,
            totalTasks = todoCount + inProgressCount + doneCount,
            todoCount = todoCount,
            inProgressCount = inProgressCount,
            doneCount = doneCount,
            overdueCount = overdueCount
        )
    }

    /**
     * Verifies that the project exists and is owned by the requesting user.
     * Throws ResourceNotFoundException (404) or UnauthorizedActionException (403).
     */
    @Transactional(readOnly = true)
    fun getVerifiedProject(projectId: Long, currentUserId: Long): Project {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResourceNotFoundException("Project not found with id: $projectId") }

        if (project.owner.id != currentUserId) {
            throw UnauthorizedActionException("Access denied: You do not own project id $projectId")
        }

        return project
    }
}

