package com.taskflow.repository

import com.taskflow.model.Task
import com.taskflow.model.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TaskRepository : JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    fun countByProjectIdAndStatus(projectId: Long, status: TaskStatus): Long

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status != com.taskflow.model.TaskStatus.DONE AND t.dueDate IS NOT NULL AND t.dueDate < :today")
    fun countOverdueTasks(@Param("projectId") projectId: Long, @Param("today") today: LocalDate): Long
}

