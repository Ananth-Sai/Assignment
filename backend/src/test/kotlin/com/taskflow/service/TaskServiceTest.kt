package com.taskflow.service

import com.taskflow.dto.CreateTaskRequest
import com.taskflow.dto.UpdateTaskRequest
import com.taskflow.dto.UpdateTaskStatusRequest
import com.taskflow.exception.BadRequestException
import com.taskflow.exception.UnauthorizedActionException
import com.taskflow.mapper.TaskMapper
import com.taskflow.model.Project
import com.taskflow.model.Task
import com.taskflow.model.TaskPriority
import com.taskflow.model.TaskStatus
import com.taskflow.model.User
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.UserRepository
import com.taskflow.strategy.TaskFilterStrategy
import com.taskflow.strategy.TaskSortStrategy
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.Optional

class TaskServiceTest {

    private val taskRepository: TaskRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val projectService: ProjectService = mockk()
    private val taskMapper: TaskMapper = TaskMapper()
    private val filterStrategy: TaskFilterStrategy = mockk()
    private val sortStrategy: TaskSortStrategy = mockk()

    private lateinit var taskService: TaskService

    private val ownerUser = User(id = 1L, name = "Owner", email = "owner@test.com", password = "hash")
    private val otherUser = User(id = 2L, name = "Other", email = "other@test.com", password = "hash")
    private val testProject = Project(id = 10L, name = "Test Project", description = "Test Desc", owner = ownerUser)

    @BeforeEach
    fun setUp() {
        taskService = TaskService(
            taskRepository,
            userRepository,
            projectService,
            taskMapper,
            filterStrategy,
            sortStrategy
        )
    }

    @Test
    @DisplayName("createTask: throws BadRequestException when dueDate is in the past")
    fun createTask_dueDateInPast_throwsBadRequest() {
        val pastDate = LocalDate.now().minusDays(2)
        val request = CreateTaskRequest(
            title = "Task with past due date",
            dueDate = pastDate
        )

        every { projectService.getVerifiedProject(10L, 1L) } returns testProject

        val exception = assertThrows<BadRequestException> {
            taskService.createTask(projectId = 10L, currentUserId = 1L, request = request)
        }

        assertTrue(exception.message!!.contains("Due date cannot be in the past"))
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    @DisplayName("createTask: successfully saves and returns DTO when dueDate is in the future")
    fun createTask_validFutureDate_succeeds() {
        val futureDate = LocalDate.now().plusDays(5)
        val request = CreateTaskRequest(
            title = "Valid future task",
            priority = TaskPriority.HIGH,
            dueDate = futureDate
        )

        every { projectService.getVerifiedProject(10L, 1L) } returns testProject
        every { taskRepository.save(any()) } answers {
            val savedTask = firstArg<Task>()
            savedTask.id = 100L
            savedTask
        }

        val result = taskService.createTask(projectId = 10L, currentUserId = 1L, request = request)

        assertNotNull(result)
        assertEquals(100L, result.id)
        assertEquals("Valid future task", result.title)
        assertEquals(TaskStatus.TODO, result.status)
        assertEquals(TaskPriority.HIGH, result.priority)
        assertFalse(result.isOverdue)
        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    @DisplayName("updateTask: throws UnauthorizedActionException if task's project belongs to someone else")
    fun updateTask_notOwner_throwsUnauthorizedActionException() {
        val foreignProject = Project(id = 20L, name = "Foreign Project", owner = otherUser)
        val existingTask = Task(
            id = 50L,
            title = "Foreign Task",
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            project = foreignProject
        )

        every { taskRepository.findById(50L) } returns Optional.of(existingTask)

        val request = UpdateTaskRequest(
            title = "Hacked title",
            status = TaskStatus.DONE,
            priority = TaskPriority.LOW
        )

        // User 1 tries to edit User 2's task
        assertThrows<UnauthorizedActionException> {
            taskService.updateTask(taskId = 50L, currentUserId = 1L, request = request)
        }

        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    @DisplayName("updateTaskStatus: successfully modifies status and returns updated DTO")
    fun updateTaskStatus_validOwner_succeeds() {
        val existingTask = Task(
            id = 55L,
            title = "Board card to drag",
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            project = testProject
        )

        every { taskRepository.findById(55L) } returns Optional.of(existingTask)
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = taskService.updateTaskStatus(
            taskId = 55L,
            currentUserId = 1L,
            request = UpdateTaskStatusRequest(status = TaskStatus.IN_PROGRESS)
        )

        assertEquals(TaskStatus.IN_PROGRESS, result.status)
        verify(exactly = 1) { taskRepository.save(match { it.status == TaskStatus.IN_PROGRESS }) }
    }
}

