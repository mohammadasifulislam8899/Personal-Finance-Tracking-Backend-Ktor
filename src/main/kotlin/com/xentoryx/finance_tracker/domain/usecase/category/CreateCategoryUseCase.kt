package com.xentoryx.finance_tracker.domain.usecase.category

import com.xentoryx.finance_tracker.domain.model.Category
import com.xentoryx.finance_tracker.domain.model.CategoryType
import com.xentoryx.finance_tracker.domain.repository.category.CategoryRepository
import com.xentoryx.finance_tracker.presentation.dto.request.CreateCategoryRequest
import java.util.UUID

class CreateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(userId: UUID, req: CreateCategoryRequest): Category {

        if (req.name.isBlank())
            throw IllegalArgumentException("Category name cannot be empty")

        val type = runCatching { CategoryType.valueOf(req.type.uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid type. Must be INCOME or EXPENSE") }

        if (categoryRepository.existsByName(userId, req.name.trim(), type.name))
            throw IllegalArgumentException("Category '${req.name}' already exists")

        return categoryRepository.create(
            Category(
                id       = UUID.randomUUID(),
                userId   = userId,
                parentId = req.parentId?.let { UUID.fromString(it) },
                name     = req.name.trim(),
                type     = type,
                icon     = req.icon?.trim(),
                color    = req.color?.trim(),
                isSystem = false
            )
        )
    }
}