package com.xentoryx.finance_tracker.presentation.mapper

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.presentation.dto.response.CategoryResponse

fun Category.toCategoryResponse() = CategoryResponse(
    id       = id.toString(),
    name     = name,
    type     = type.name,
    parentId = parentId?.toString(),
    icon     = icon,
    color    = color,
    isSystem = isSystem
)

fun List<Category>.toCategoryResponseList() = map { it.toCategoryResponse() }