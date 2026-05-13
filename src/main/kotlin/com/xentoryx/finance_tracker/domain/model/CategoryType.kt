package com.xentoryx.finance_tracker.domain.model

import java.util.UUID

enum class CategoryType { INCOME, EXPENSE }

data class Category(
    val id: UUID,
    val userId: UUID?,          // null = system default category
    val parentId: UUID?,
    val name: String,
    val type: CategoryType,
    val icon: String?,
    val color: String?,
    val isSystem: Boolean
)