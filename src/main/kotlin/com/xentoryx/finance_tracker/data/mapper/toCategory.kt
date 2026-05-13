package com.xentoryx.finance_tracker.data.mapper

import com.xentoryx.finance_tracker.data.table.Categories
import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.model.CategoryType
import org.jetbrains.exposed.v1.core.ResultRow

// ==========================
// Category Mapper
// ==========================

fun ResultRow.toCategory(): Category {
    return Category(
        id       = this[Categories.id].value,
        userId   = this[Categories.userId]?.value,
        parentId = this[Categories.parentId]?.value,
        name     = this[Categories.name],
        type     = CategoryType.valueOf(this[Categories.type]),
        icon     = this[Categories.icon],
        color    = this[Categories.color],
        isSystem = this[Categories.isSystem]
    )
}

fun Iterable<ResultRow>.toCategories(): List<Category> = map { it.toCategory() }