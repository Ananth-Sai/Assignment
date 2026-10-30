package com.taskflow.controller

import com.taskflow.dto.TaskResponseDto
import com.taskflow.dto.UpdateTaskRequest
import com.taskflow.dto.UpdateTaskStatusRequest
import com.taskflow.security.UserPrincipal
import com.taskflow.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for updating, changing status, and deleting individual tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    fun updateTask(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<TaskResponseDto> {
        val task = taskService.updateTask(id, principal.id, request)
        return ResponseEntity.ok(task)
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Quickly update only the status of a task (e.g., when moved across Kanban columns)")
    fun updateTaskStatus(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateTaskStatusRequest
    ): ResponseEntity<TaskResponseDto> {
        val task = taskService.updateTaskStatus(id, principal.id, request)
        return ResponseEntity.ok(task)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    fun deleteTask(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Map<String, String>> {
        taskService.deleteTask(taskId = id, currentUserId = principal.id)
        return ResponseEntity.ok(mapOf("message" to "Task id $id successfully deleted"))
    }
}

