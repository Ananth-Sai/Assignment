package com.taskflow.mapper

import com.taskflow.dto.ProjectResponseDto
import com.taskflow.dto.UserSummaryDto
import com.taskflow.model.Project
import org.springframework.stereotype.Component

/**
 * Design Pattern: Factory / Mapper Pattern
 * Encapsulates the transformation logic from JPA Project entity to ProjectResponseDto.
 */
@Component
class ProjectMapper {

    fun toResponseDto(project: Project): ProjectResponseDto {
        return ProjectResponseDto(
            id = project.id!!,
            name = project.name,
            description = project.description,
            owner = UserSummaryDto(
                id = project.owner.id!!,
                name = project.owner.name,
                email = project.owner.email
            ),
            createdAt = project.createdAt
        )
    }

    fun toResponseDtoList(projects: List<Project>): List<ProjectResponseDto> {
        return projects.map { toResponseDto(it) }
    }
}

