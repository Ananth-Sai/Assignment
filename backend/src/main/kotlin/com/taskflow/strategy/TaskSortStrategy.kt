package com.taskflow.strategy

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

/**
 * Design Pattern: Strategy Pattern (Sort Strategy)
 * Selects the appropriate sorting algorithm for task queries based on user request.
 */
@Component
class TaskSortStrategy {

    fun resolveSort(sortBy: String?, sortDirection: String?): Sort {
        val direction = if (sortDirection?.equals("desc", ignoreCase = true) == true) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }

        return when (sortBy?.lowercase()) {
            "duedate", "due_date" -> {
                // Secondary sort by createdAt so deterministic ordering is maintained
                Sort.by(direction, "dueDate").and(Sort.by(Sort.Direction.ASC, "createdAt"))
            }
            "priority" -> {
                Sort.by(direction, "priority").and(Sort.by(Sort.Direction.ASC, "createdAt"))
            }
            "title" -> {
                Sort.by(direction, "title")
            }
            else -> {
                // Default sorting: created date descending
                Sort.by(Sort.Direction.DESC, "createdAt")
            }
        }
    }
}

