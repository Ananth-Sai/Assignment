package com.taskflow.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.taskflow.dto.CreateProjectRequest
import com.taskflow.dto.CreateTaskRequest
import com.taskflow.dto.LoginRequest
import com.taskflow.dto.RegisterRequest
import com.taskflow.model.TaskPriority
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class TaskFlowIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("End-to-End Integration Flow: Register -> Login -> Create Project -> Create Task -> Summary & Validation")
    fun testEndToEndProjectAndTaskWorkflow() {
        val uniqueEmail = "integration.tester.${System.currentTimeMillis()}@taskflow.com"

        // 1. Register a new user
        val registerReq = RegisterRequest(
            name = "Integration Tester",
            email = uniqueEmail,
            password = "SecretPassword123!"
        )

        val registerResult = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerReq)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.token") { exists() }
            jsonPath("$.user.email") { value(uniqueEmail) }
        }.andReturn()

        // 2. Login to get fresh token
        val loginReq = LoginRequest(
            email = uniqueEmail,
            password = "SecretPassword123!"
        )

        val loginResult = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginReq)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { exists() }
        }.andReturn()

        val tokenJson = objectMapper.readTree(loginResult.response.contentAsString)
        val jwtToken = tokenJson.get("token").asText()
        val authHeader = "Bearer $jwtToken"

        // 3. Unauthenticated request to /api/projects should be rejected
        mockMvc.get("/api/projects")
            .andExpect {
                status { isForbidden() }
            }

        // 4. Create a Project with valid JWT
        val projectReq = CreateProjectRequest(
            name = "E2E Mobile App Project",
            description = "Test project for automated integration verification"
        )

        val projectResult = mockMvc.post("/api/projects") {
            header("Authorization", authHeader)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(projectReq)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.name") { value("E2E Mobile App Project") }
        }.andReturn()

        val projectId = objectMapper.readTree(projectResult.response.contentAsString).get("id").asLong()

        // 5. Test validation rule: dueDate in past should return 400 Bad Request
        val invalidTaskReq = CreateTaskRequest(
            title = "Task with past due date",
            dueDate = LocalDate.now().minusDays(3)
        )

        mockMvc.post("/api/projects/$projectId/tasks") {
            header("Authorization", authHeader)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidTaskReq)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("Due date cannot be in the past when creating a task") }
        }

        // 6. Create a valid Task with future due date
        val validTaskReq = CreateTaskRequest(
            title = "Design Figma mockups",
            description = "Wireframes for mobile navigation",
            priority = TaskPriority.HIGH,
            dueDate = LocalDate.now().plusDays(7)
        )

        mockMvc.post("/api/projects/$projectId/tasks") {
            header("Authorization", authHeader)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validTaskReq)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.title") { value("Design Figma mockups") }
            jsonPath("$.isOverdue") { value(false) }
        }

        // 7. Verify project summary
        mockMvc.get("/api/projects/$projectId/summary") {
            header("Authorization", authHeader)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalTasks") { value(1) }
            jsonPath("$.todoCount") { value(1) }
            jsonPath("$.overdueCount") { value(0) }
        }
    }
}

