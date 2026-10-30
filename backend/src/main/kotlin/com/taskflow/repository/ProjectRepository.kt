package com.taskflow.repository

import com.taskflow.model.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByOwnerIdOrderByCreatedAtDesc(ownerId: Long): List<Project>
    fun findByIdAndOwnerId(id: Long, ownerId: Long): Optional<Project>
}

