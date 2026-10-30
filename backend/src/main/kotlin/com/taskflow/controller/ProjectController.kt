package com.taskflow.controller

import com.taskflow.dto.CreateProjectRequest
import com.taskflow.dto.CreateTaskRequest
import com.taskflow.dto.PageResponse
import com.taskflow.dto.ProjectResponseDto
import com.taskflow.dto.ProjectSummaryDto
import com.taskflow.dto.TaskResponseDto
import com.taskflow.model.TaskPriority
import com.taskflow.model.TaskStatus
import com.taskflow.security.UserPrincipal
import com.taskflow.service.ProjectService
import com.taskflow.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Endpoints for managing user projects, tasks, and project summaries")
class ProjectController(
    private val projectService: ProjectService,
    private val taskService: TaskService
) {

    @GetMapping
    @Operation(summary = "List all projects owned by the currently authenticated user")
    fun getProjects(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ProjectResponseDto>> {
        val projects = projectService.getProjectsForUser(principal.id)
        return ResponseEntity.ok(projects)
    }

    @PostMapping
    @Operation(summary = "Create a new project owned by the currently authenticated user")
    fun createProject(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateProjectRequest
    ): ResponseEntity<ProjectResponseDto> {
        val project = projectService.createProject(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(project)
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get task count per status and overdue count for a specific project")
    fun getProjectSummary(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ProjectSummaryDto> {
        val summary = projectService.getProjectSummary(id, principal.id)
        return ResponseEntity.ok(summary)
    }

    @GetMapping("/{id}/tasks")
    @Operation(summary = "List tasks for a project with filters, sorting, and pagination")
    fun getTasks(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(required = false) status: TaskStatus?,
        @RequestParam(required = false) priority: TaskPriority?,
        @RequestParam(required = false) assigneeId: Long?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortDirection: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<PageResponse<TaskResponseDto>> {
        val tasks = taskService.getTasksForProject(
            projectId = id,
            currentUserId = principal.id,
            status = status,
            priority = priority,
            assigneeId = assigneeId,
            sortBy = sortBy,
            sortDirection = sortDirection,
            page = page,
            size = size
        )
        return ResponseEntity.ok(tasks)
    }

    @PostMapping("/{id}/tasks")
    @Operation(summary = "Create a new task within a project")
    fun createTask(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateTaskRequest
    ): ResponseEntity<TaskResponseDto> {
        val task = taskService.createTask(id, principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(task)
    }
}

