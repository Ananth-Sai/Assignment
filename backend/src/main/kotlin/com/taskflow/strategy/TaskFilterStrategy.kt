package com.taskflow.strategy

import com.taskflow.model.Task
import com.taskflow.model.TaskPriority
import com.taskflow.model.TaskStatus
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

/**
 * Design Pattern: Strategy Pattern (Filter Strategy)
 * Dynamically builds JPA Specifications based on user filter criteria.
 */
@Component
class TaskFilterStrategy {

    fun buildSpecification(
        projectId: Long,
        status: TaskStatus?,
        priority: TaskPriority?,
        assigneeId: Long?
    ): Specification<Task> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            // Project boundary constraint (Mandatory)
            predicates.add(cb.equal(root.get<Any>("project").get<Long>("id"), projectId))

            // Optional status filter strategy
            status?.let {
                predicates.add(cb.equal(root.get<TaskStatus>("status"), it))
            }

            // Optional priority filter strategy
            priority?.let {
                predicates.add(cb.equal(root.get<TaskPriority>("priority"), it))
            }

            // Optional assignee filter strategy
            assigneeId?.let {
                predicates.add(cb.equal(root.get<Any>("assignee").get<Long>("id"), it))
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}

