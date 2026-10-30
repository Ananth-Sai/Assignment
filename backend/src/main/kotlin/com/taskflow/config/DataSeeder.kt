package com.taskflow.config

import com.taskflow.model.Project
import com.taskflow.model.Task
import com.taskflow.model.TaskPriority
import com.taskflow.model.TaskStatus
import com.taskflow.model.User
import com.taskflow.repository.ProjectRepository
import com.taskflow.repository.TaskRepository
import com.taskflow.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DataSeeder(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DataSeeder::class.java)

    override fun run(vararg args: String?) {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded. Skipping initial data creation.")
            return
        }

        logger.info("Seeding initial demo data for TaskFlow...")

        // 1. Create Demo User
        val demoUser = userRepository.save(
            User(
                name = "Demo Engineer",
                email = "demo@taskflow.com",
                password = passwordEncoder.encode("Password123!")
            )
        )

        val teamMember = userRepository.save(
            User(
                name = "Alex Developer",
                email = "alex@taskflow.com",
                password = passwordEncoder.encode("Password123!")
            )
        )

        // 2. Create Sample Project
        val sampleProject = projectRepository.save(
            Project(
                name = "TaskFlow 2.0 Web Launch",
                description = "Modernizing our core task management application with Kotlin and React.",
                owner = demoUser
            )
        )

        // 3. Create Sample Tasks across the 3 columns (including an overdue one)
        // Task 1: Overdue Task (due yesterday, status TODO)
        taskRepository.save(
            Task(
                title = "Migrate database schema to PostgreSQL",
                description = "Review foreign keys, unique indices on user email, and configure migration tool.",
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                dueDate = LocalDate.now().minusDays(1), // Overdue!
                assignee = demoUser,
                project = sampleProject
            )
        )

        // Task 2: In-Progress Task (due tomorrow)
        taskRepository.save(
            Task(
                title = "Implement JWT authentication filter",
                description = "Secure all endpoints using stateless JWT bearer tokens with Spring Security.",
                status = TaskStatus.IN_PROGRESS,
                priority = TaskPriority.MEDIUM,
                dueDate = LocalDate.now().plusDays(2),
                assignee = demoUser,
                project = sampleProject
            )
        )

        // Task 3: Done Task (completed)
        taskRepository.save(
            Task(
                title = "Project architecture kickoff",
                description = "Agreed on layered structure: Controller -> Service -> Repository with DTO mappers.",
                status = TaskStatus.DONE,
                priority = TaskPriority.LOW,
                dueDate = LocalDate.now().minusDays(3),
                assignee = teamMember,
                project = sampleProject
            )
        )

        logger.info("Successfully seeded demo data: User 'demo@taskflow.com' (password: 'Password123!') with sample project and tasks.")
    }
}

