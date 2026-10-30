package com.taskflow.service

import com.taskflow.exception.UnauthorizedActionException
import com.taskflow.mapper.ProjectMapper
import com.taskflow.model.Project
import com.taskflow.model.TaskStatus
import com.taskflow.model.User
import com.taskflow.repository.ProjectRepository
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class ProjectServiceTest {

    private val projectRepository: ProjectRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val projectMapper: ProjectMapper = ProjectMapper()

    private lateinit var projectService: ProjectService

    private val ownerUser = User(id = 1L, name = "Owner", email = "owner@test.com", password = "hash")
    private val otherUser = User(id = 2L, name = "Other", email = "other@test.com", password = "hash")
    private val testProject = Project(id = 10L, name = "Project 10", description = "Test", owner = ownerUser)

    @BeforeEach
    fun setUp() {
        projectService = ProjectService(
            projectRepository,
            userRepository,
            taskRepository,
            projectMapper
        )
    }

    @Test
    @DisplayName("getProjectSummary: correctly aggregates status counts and overdue count")
    fun getProjectSummary_success() {
        every { projectRepository.findById(10L) } returns Optional.of(testProject)
        every { taskRepository.countByProjectIdAndStatus(10L, TaskStatus.TODO) } returns 3L
        every { taskRepository.countByProjectIdAndStatus(10L, TaskStatus.IN_PROGRESS) } returns 2L
        every { taskRepository.countByProjectIdAndStatus(10L, TaskStatus.DONE) } returns 5L
        every { taskRepository.countOverdueTasks(10L, any()) } returns 1L

        val summary = projectService.getProjectSummary(projectId = 10L, currentUserId = 1L)

        assertEquals(10L, summary.projectId)
        assertEquals(10L, summary.totalTasks) // 3 + 2 + 5
        assertEquals(3L, summary.todoCount)
        assertEquals(2L, summary.inProgressCount)
        assertEquals(5L, summary.doneCount)
        assertEquals(1L, summary.overdueCount)
    }

    @Test
    @DisplayName("getProjectSummary: throws UnauthorizedActionException when user does not own project")
    fun getProjectSummary_unauthorizedUser_throws() {
        every { projectRepository.findById(10L) } returns Optional.of(testProject)

        // User 2 tries to fetch User 1's summary
        assertThrows<UnauthorizedActionException> {
            projectService.getProjectSummary(projectId = 10L, currentUserId = 2L)
        }
    }
}

